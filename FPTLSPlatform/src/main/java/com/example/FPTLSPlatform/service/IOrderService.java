package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderService {
    OrderDTO createOrder(Long classId, String username) throws Exception;

    Page<OrderDTO> getOrdersByUser(String username, Pageable pageable) throws Exception;

    ResponseDTO<String> cancelOrder(Long orderId) throws Exception;

    Page<OrderDTO> getAllOrders(Pageable pageable);

    Page<ClassDTO> getClassesOrderedByUser(String username, Pageable pageable);
}
