package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.TransactionHistoryDTO;
import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.Wallet;

import java.util.List;

public interface IWalletService {
    List<TransactionHistoryDTO> getTransactionHistory() throws Exception;

    Wallet getWalletByUserName() throws Exception;

    void refundToWallet(Long amount) throws Exception;

    List<WalletStatisticDTO> getWalletStatistic(Integer year);
    Wallet getWalletByTeacherName() throws Exception;
}
