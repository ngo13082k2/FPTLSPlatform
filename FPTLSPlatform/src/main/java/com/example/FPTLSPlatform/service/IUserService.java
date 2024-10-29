package com.example.FPTLSPlatform.service;

import java.util.Map;

public interface IUserService {
    void updateWalletBalance(String username, Double amount);
    Double getSystemWalletBalance();
    Map<String, Long> getUserCountByRole();
}
