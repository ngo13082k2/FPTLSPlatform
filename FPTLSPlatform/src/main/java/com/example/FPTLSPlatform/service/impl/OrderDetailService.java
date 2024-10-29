package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.OrderDetailDTO;
import com.example.FPTLSPlatform.exception.ScheduleDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.OrderDetail;
import com.example.FPTLSPlatform.repository.OrderDetailRepository;
import com.example.FPTLSPlatform.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class OrderDetailService implements IOrderDetailService {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public Page<OrderDetailDTO> getOrderDetailsByOrderId(Long orderId, Pageable pageable) {
        Page<OrderDetail> orderDetails = orderDetailRepository.findOrderDetailsByOrderId(orderId, pageable);


        return orderDetails.map(orderDetail -> OrderDetailDTO.builder()
                .orderDetailId(orderDetail.getOrderDetailId())
                .orderId(orderDetail.getOrder().getOrderId())
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
                        .build())
                .scheduleDTO(ScheduleDTO.builder()
                        .scheduleId(orderDetail.getSchedules().getScheduleId())
                        .slotId(orderDetail.getSchedules().getSlot().getSlotId())
                        .startDate(orderDetail.getSchedules().getStartDate())
                        .endDate(orderDetail.getSchedules().getEndDate())
                        .classId(orderDetail
                                .getSchedules()
                                .getClasses()
                                .stream()
                                .map(Class::getClassId)
                                .collect(Collectors.toList()))
                        .build())
                .price(orderDetail.getPrice())
                .build());
    }

}
