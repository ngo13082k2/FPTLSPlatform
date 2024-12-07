package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.OrderDetailDTO;

import com.example.FPTLSPlatform.dto.StudentDTO;
import com.example.FPTLSPlatform.model.OrderDetail;
import com.example.FPTLSPlatform.repository.ClassRepository;
import com.example.FPTLSPlatform.repository.OrderDetailRepository;
import com.example.FPTLSPlatform.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OrderDetailService implements IOrderDetailService {


    private final OrderDetailRepository orderDetailRepository;

    private final ClassRepository classRepository;

    @Autowired
    public OrderDetailService(OrderDetailRepository orderDetailRepository, ClassRepository classRepository) {
        this.orderDetailRepository = orderDetailRepository;
        this.classRepository = classRepository;
    }

    @Override
    public Page<OrderDetailDTO> getOrderDetailsByOrderId(Long orderId, Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findOrderDetailsByOrderId(orderId, pageable);


        return orderDetails.map(orderDetail -> {
            List<StudentDTO> studentDTOS = classRepository.findStudentsByClassId(orderDetail.getClasses().getClassId())
                    .stream()
                    .map(student -> StudentDTO.builder()
                            .userName(student.getUserName())
                            .email(student.getEmail())
                            .phoneNumber(student.getPhoneNumber())
                            .address(student.getAddress())
                            .build())
                    .toList();

            return OrderDetailDTO.builder()
                    .orderDetailId(orderDetail.getOrderDetailId())
                    .orderDTO(OrderDTO.builder()
                            .orderId(orderDetail.getOrder().getOrderId())
                            .totalPrice(orderDetail.getOrder().getTotalPrice())
                            .username(orderDetail.getOrder().getUser().getUserName())
                            .createAt(orderDetail.getOrder().getCreateAt())
                            .status(orderDetail.getOrder().getStatus())
                            .build())
                    .classDTO(ClassDTO.builder()
                            .classId(orderDetail.getClasses().getClassId())
                            .name(orderDetail.getClasses().getName())
                            .code(orderDetail.getClasses().getCode())
                            .description(orderDetail.getClasses().getDescription())
                            .status(orderDetail.getClasses().getStatus())
                            .location(orderDetail.getClasses().getLocation())
                            .maxStudents(orderDetail.getClasses().getMaxStudents())
                            .createDate(orderDetail.getClasses().getCreateDate())
                            .price(orderDetail.getClasses().getPrice())
                            .teacherName(orderDetail.getClasses().getTeacher().getTeacherName())
                            .fullName(orderDetail.getClasses().getTeacher().getFullName())
                            .startDate(orderDetail.getClasses().getStartDate())
                            .courseCode(orderDetail.getClasses().getCourses().getCourseCode())
                            .imageUrl(orderDetail.getClasses().getImage())
                            .imageTeacher(orderDetail.getClasses().getTeacher().getAvatarImage())
                            .students(studentDTOS)
                            .build())
                    .price(orderDetail.getPrice())
                    .build();
        });
    }
}
