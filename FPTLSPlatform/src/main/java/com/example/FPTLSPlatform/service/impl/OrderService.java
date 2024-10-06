package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.*;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.repository.*;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.IVNPayService;
import com.example.FPTLSPlatform.service.IWalletService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {
    private final OrderRepository orderRepository;

    private final ClassRepository classRepository;

    private final OrderDetailRepository orderDetailRepository;

    private final UserRepository userRepository;

    private final IVNPayService vnPayService;

    private final IWalletService walletService;


    public OrderService(OrderRepository orderRepository,
                        ClassRepository classRepository,
                        OrderDetailRepository orderDetailRepository,
                        UserRepository userRepository,
                        IVNPayService vnPayService,
                        IWalletService walletService) {
        this.orderRepository = orderRepository;
        this.classRepository = classRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.vnPayService = vnPayService;
        this.walletService = walletService;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @Override
    public OrderDTO createOrder(Long classId, String username) throws Exception {
        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }

        Class aClass = classOptional.get();

        Optional<User> userOptional = userRepository.findById(username);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with username: " + username);
        }
        boolean hasOrdered = orderDetailRepository.existsByOrder_User_UserNameAndClasses_ClassId(username, classId);
        if (hasOrdered) {
            throw new Exception("User has already registered for this class.");
        }
        User user = userOptional.get();
        Wallet wallet = walletService.getWalletByUserName();

        if (wallet.getBalance() < aClass.getPrice()) {
            throw new Exception("Wallet balance is not enough to register for class.");
        }

        double newBalance = wallet.getBalance() - aClass.getPrice();
        wallet.setBalance(newBalance);

        userRepository.save(wallet.getUser());

        Order order = new Order();
        order.setUser(user);
        order.setCreateAt(LocalDateTime.now());
        order.setStatus("PENDING");
        order.setTotalPrice(aClass.getPrice());
        order = orderRepository.save(order);

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrder(order);
        orderDetail.setClasses(aClass);
        orderDetail.setPrice(aClass.getPrice());
        orderDetailRepository.save(orderDetail);
        checkAndActivateClass(classId);

        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .username(order.getUser().getUserName())
                .createAt(order.getCreateAt())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .build();
    }


    @Override
    public boolean checkAndActivateClass(Long classId) throws ResourceNotFoundException {
        int registeredStudents = orderDetailRepository.countByClasses_ClassId(classId);

        Optional<Class> classOptional = classRepository.findById(classId);
        if (classOptional.isEmpty()) {
            throw new ResourceNotFoundException("Class not found with id: " + classId);
        }

        Class aClass = classOptional.get();

        if (registeredStudents >= aClass.getMaxStudents()*0.5) {
            aClass.setStatus("ACTIVE");
            classRepository.save(aClass);
            return true;
        }
        return false;
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
    public ResponseDTO<String> cancelOrder(Long orderId) {
        try {
            Order order = orderRepository.findById(String.valueOf(orderId))
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

            // Check if the order is already cancelled
            if (Objects.equals(order.getStatus(), "CANCELLED")) {
                return new ResponseDTO<>("ERROR", "Cannot cancel the order because the order has already been cancelled.", null);
            }

            OrderDetail orderDetail = orderDetailRepository.findByOrder_OrderId(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order detail not found for order id: " + orderId));

            Class aClass = orderDetail.getClasses();

            if (aClass.getEndDate() == null) {
                return new ResponseDTO<>("ERROR", "Class end date is missing.", null);
            }

            // Check if the class has already ended
            if (aClass.getEndDate().isBefore(LocalDateTime.now())) {
                return new ResponseDTO<>("ERROR", "Cannot cancel the order because the class has already ended.", null);
            }

            // Check if the order status is PENDING
            if (Objects.equals(order.getStatus(), "PENDING")) {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime cancelDeadline = aClass.getEndDate().minusDays(2);

                // Check if cancellation is within 2 days of class ending
                if (now.isAfter(cancelDeadline)) {
                    return new ResponseDTO<>("ERROR", "Cannot cancel the order within 2 days before the class ends.", null);
                }

                // Refund and update order status
                walletService.refundToWallet(order.getTotalPrice());
                order.setStatus("CANCELLED");
                orderRepository.save(order);

                return new ResponseDTO<>("SUCCESS", "Order cancelled successfully", null);              }

            return new ResponseDTO<>("ERROR", "Cannot cancel the order with current status.", null);

        } catch (Exception ex) {
            return new ResponseDTO<>("ERROR", ex.getMessage(), null);
        }
    }
}
