package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.repository.SystemTransactionHistoryRepository;
import com.example.FPTLSPlatform.service.ISystemWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemWalletService implements ISystemWalletService {

    @Autowired
    private SystemTransactionHistoryRepository systemTransactionHistoryRepository;

    public List<SystemTransactionHistory> getSystemWalletTransactionHistory() {
        return systemTransactionHistoryRepository.findAll();
    }
}