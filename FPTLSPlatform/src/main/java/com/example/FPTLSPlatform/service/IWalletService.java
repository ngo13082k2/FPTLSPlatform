package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.Wallet;

import java.util.List;

public interface IWalletService {
    List<TransactionHistory> getTransactionHistory() throws Exception;
    Wallet getWalletByUserName() throws Exception;
    void refundToWallet(Long amount) throws Exception;
}
