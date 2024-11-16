package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryDTO {
    private Long id;
    private double amount;
    private LocalDateTime transactionDate;
    private double transactionBalance;
    private String userName;
    private String teacherName;
    private String note;

}
