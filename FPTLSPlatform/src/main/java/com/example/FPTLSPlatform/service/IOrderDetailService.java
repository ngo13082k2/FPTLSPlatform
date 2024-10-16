package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OrderDetailDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Order;
import com.example.FPTLSPlatform.model.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IOrderDetailService {
    Page<OrderDetailDTO> getOrderDetailsByOrderId(Long orderId, Pageable pageable);
}
