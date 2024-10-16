package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.OrderDetailDTO;
import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Order;
import com.example.FPTLSPlatform.model.OrderDetail;
import com.example.FPTLSPlatform.repository.OrderDetailRepository;
import com.example.FPTLSPlatform.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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
                .classId(orderDetail.getClasses().getClassId())
                .price(orderDetail.getPrice())
                .build());
    }

}
