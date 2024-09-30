package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping("/{classId}/{username}")
    public ResponseEntity<OrderDTO> createOrder(@PathVariable Long classId, @PathVariable String username) {
        try {
            OrderDTO orderDTO = orderService.createOrder(classId, username);
            return new ResponseEntity<>(orderDTO, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<List<OrderDTO>> getOrdersByUser(@PathVariable String username) {
        List<OrderDTO> orders = orderService.getOrdersByUser(username);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        orderService.cancelOrder(orderId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}