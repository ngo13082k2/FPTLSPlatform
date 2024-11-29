package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WalletStatisticDTO {
    private int month;
    private Double totalBalance;
    private Double totalIncome;
    private Double totalExpenses;
    private Long totalOrders;
}
