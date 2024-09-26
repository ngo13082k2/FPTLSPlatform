package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Order;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.OrderDetail;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.OrderDetailRepository;
import com.example.FPTLSPlatform.repository.OrderRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClassRepository classRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public OrderDTO createOrder(Long classId, String username) throws ResourceNotFoundException {
        // 1. Tìm lớp học dựa trên classId
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }

        Class aClass = classOptional.get();

        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with username: " + username);

        }
        // 2. Tạo một Order mới
        Order order = new Order();
        order.setUser(userOptional.get());
        order.setCreateAt(LocalDateTime.now());
        order.setStatus("PENDING");

        // 3. Tính toán giá và lưu Order
        order.setTotalPrice(aClass.getPrice());
        order = orderRepository.save(order);

        // 4. Tạo chi tiết đơn hàng

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setClasses(classOptional.get());
        orderDetail.setPrice(aClass.getPrice());
        orderDetailRepository.save(orderDetail);

        // 5. Kiểm tra nếu đã đủ học sinh để mở lớp
        checkAndActivateClass(classId);

        // 6. Trả về DTO cho Order
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .username(order.getUser().getUserName())
                .createAt(order.getCreateAt())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }

    @Override
    public void checkAndActivateClass(Long classId) throws ResourceNotFoundException {
        // 1. Tìm số lượng học sinh đã đăng ký vào lớp này
        int registeredStudents = orderDetailRepository.countByClasses_ClassId(classId);

        // 2. Tìm lớp học
        Optional<Class> classOptional = classRepository.findById(classId);
        if (!classOptional.isPresent()) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }

        Class aClass = classOptional.get();

        // 3. Nếu số lượng học sinh >= số lượng tối thiểu, mở lớp
        if (registeredStudents >= aClass.getMaxStudents()*0.5) {
            aClass.setStatus("ACTIVE");
            classRepository.save(aClass);
        }
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String username) {
        List<Order> orders = orderRepository.findByUserName(username);
        return orders.stream()
                .map(order -> OrderDTO.builder()
                        .orderId(order.getOrderId())
                        .username(order.getUser().getUserName())
                        .createAt(order.getCreateAt())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(String.valueOf(orderId))
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }
}
