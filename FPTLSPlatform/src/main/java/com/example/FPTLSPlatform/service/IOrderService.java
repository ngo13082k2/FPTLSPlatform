package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;

import java.util.List;

public interface IOrderService {
    OrderDTO createOrder(Long classId, String username) throws Exception;
    List<OrderDTO> getOrdersByUser(String username);
    ResponseDTO<String> cancelOrder(Long orderId) throws Exception;
}
