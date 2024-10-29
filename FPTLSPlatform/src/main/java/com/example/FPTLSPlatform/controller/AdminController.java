package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.service.ISystemWalletService;
import com.example.FPTLSPlatform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IUserService userService;
    @Autowired
    private ISystemWalletService systemWalletService;

    @GetMapping("/system-wallet/balance")
    public Double getSystemWalletBalance() {
        return userService.getSystemWalletBalance();
    }

    @GetMapping("/system-wallet/transactions")
    public List<SystemTransactionHistory> getSystemWalletTransactionHistory() {
        return systemWalletService.getSystemWalletTransactionHistory();
    }
}
