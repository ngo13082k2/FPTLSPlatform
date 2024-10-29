package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import com.example.FPTLSPlatform.model.SystemWallet;

import java.util.List;

public interface ISystemWalletService {
    List<SystemTransactionHistory> getSystemWalletTransactionHistory();
}
