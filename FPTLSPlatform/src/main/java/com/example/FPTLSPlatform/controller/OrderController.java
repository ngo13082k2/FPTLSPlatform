package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OrderDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.service.IOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    public OrderController(IOrderService orderService) {
        this.orderService = orderService;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @GetMapping()
    public ResponseEntity<ResponseDTO<Page<OrderDTO>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<OrderDTO> orders = orderService.getAllOrders(PageRequest.of(page, size));
        ResponseDTO<Page<OrderDTO>> response = new ResponseDTO<>("SUCCESS", "Get All successfully", orders);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/{classId}")
    public ResponseEntity<ResponseDTO<OrderDTO>> createOrder(@PathVariable Long classId) {
        try {
            String username = getCurrentUsername();
            OrderDTO orderDTO = orderService.createOrder(classId, username);
            ResponseDTO<OrderDTO> response = new ResponseDTO<>("SUCCESS", "Order created successfully", orderDTO);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            ResponseDTO<OrderDTO> response = new ResponseDTO<>("ERROR", "Class not found", null);
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            ResponseDTO<OrderDTO> response = new ResponseDTO<>("ERROR", e.getMessage(), null);
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<ResponseDTO<Page<OrderDTO>>> getOrdersByUser(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) throws Exception {
        String username = getCurrentUsername();
        Page<OrderDTO> orders = orderService.getOrdersByUser(username, PageRequest.of(page, size));
        ResponseDTO<Page<OrderDTO>> response = new ResponseDTO<>("SUCCESS", "Orders retrieved successfully", orders);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PutMapping("/cancel/{orderId}")
    public ResponseEntity<ResponseDTO<String>> cancelOrder(@PathVariable Long orderId) throws Exception {
        ResponseDTO<String> response = orderService.cancelOrder(orderId);

        if (response.getStatus().equals("ERROR")) {
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}