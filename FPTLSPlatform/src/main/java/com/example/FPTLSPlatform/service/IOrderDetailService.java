package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OrderDetailDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Order;
import com.example.FPTLSPlatform.model.OrderDetail;

import java.util.List;

public interface IOrderDetailService {
    List<OrderDetailDTO> getOrderDetailsByOrderId(Long orderId);
}
