package com.example.FPTLSPlatform.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawalRequestDTO {
    private String accountHolderName;
    private String accountNumber;
    private String bank;
    private String idNumber;
    private Long applicationTypeId;
    private double amount;
}
