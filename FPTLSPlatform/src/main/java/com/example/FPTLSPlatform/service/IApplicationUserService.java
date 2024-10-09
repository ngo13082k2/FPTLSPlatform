package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;

public interface IApplicationUserService {
    void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto);
    void processOtherRequest(OtherApplicationDTO otherRequestDto);
}
