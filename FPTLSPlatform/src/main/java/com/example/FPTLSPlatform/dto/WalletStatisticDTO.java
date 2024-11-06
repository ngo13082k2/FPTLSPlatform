package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletStatisticDTO {
    private int month;
    private Double totalBalance;
    private Double averageBalance;
    private Double totalRefunded;
    private Double averageDeposit;
    private Double totalDeposit;
    private Double totalSpend;
}
