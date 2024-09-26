package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Order;

import java.util.List;

public interface IOrderService {
    OrderDTO createOrder(Long classId, String username) throws ResourceNotFoundException;
    void checkAndActivateClass(Long classId) throws ResourceNotFoundException;
    List<OrderDTO> getOrdersByUser(String username);
    void cancelOrder(Long orderId);
}
