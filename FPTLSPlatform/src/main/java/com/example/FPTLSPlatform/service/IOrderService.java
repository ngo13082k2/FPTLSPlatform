package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.*;
import com.example.FPTLSPlatform.model.Class;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface IOrderService {
    ListTotalOrderDTO getTotalOrders(LocalDateTime startDate, LocalDateTime endDate);

    OrderDTO createOrder(Long classId, String username) throws Exception;

    Page<OrderDTO> getOrdersByUser(String username, Pageable pageable) throws Exception;

    ResponseDTO<String> cancelOrder(Long orderId) throws Exception;

    Page<OrderDTO> getAllOrders(Pageable pageable);

    Page<OrderDetailDTO> getClassesOrderedByUser(String username, Pageable pageable);

    void refundStudents(Class cancelledClass);

    void sendActivationEmail(Class scheduledClass);

    void completeClassImmediately(Long classId);
}
