package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OrderDetailDTO;
import com.example.FPTLSPlatform.service.IOrderDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-details")
public class OrderDetailController {
    @Autowired
    private IOrderDetailService orderDetailService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Page<OrderDetailDTO>> getOrderDetailsByOrderId(@PathVariable Long orderId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {
        Page<OrderDetailDTO> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId, PageRequest.of(page, size));
        return new ResponseEntity<>(orderDetails, HttpStatus.OK);
    }
}
