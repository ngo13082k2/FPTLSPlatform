package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.TotalOrderDTO;
import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.ISystemWalletService;
import com.example.FPTLSPlatform.service.IUserService;
import com.example.FPTLSPlatform.service.impl.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISystemWalletService systemWalletService;
    @Autowired
    private IOrderService orderService;

    @GetMapping("/system-wallet/balance")
    public Double getSystemWalletBalance() {
        return userService.getSystemWalletBalance();
    }

    @GetMapping("/system-wallet/transactions")
    public List<SystemTransactionHistory> getSystemWalletTransactionHistory() {
        return systemWalletService.getSystemWalletTransactionHistory();
    }

    @GetMapping("/user-count")
    public Map<String, Long> getUserCountByRole() {
        return userService.getUserCountByRole();
    }

    @GetMapping("/total")
    public TotalOrderDTO getTotalOrdersAndAmount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return orderService.getTotalOrders(startDate, endDate);
    }
}
