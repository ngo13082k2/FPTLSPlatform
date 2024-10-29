package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.ApplicationUser;

import java.util.List;

public interface IApplicationUserService {
    void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto);
    void processOtherRequest(OtherApplicationDTO otherRequestDto);
    List<ApplicationUser> getApplicationsByType(Long applicationTypeId);
    String processWithdrawalPayment(Long applicationUserId);
    String completeWithdrawalRequest(Long applicationUserId);

}
