package com.example.FPTLSPlatform.service;

public interface IUserService {
    void updateWalletBalance(String username, Double amount);
    Double getSystemWalletBalance();
}
