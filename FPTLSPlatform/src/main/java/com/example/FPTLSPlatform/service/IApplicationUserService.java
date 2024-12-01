package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.ApplicationUser;
import com.example.FPTLSPlatform.model.ApprovalRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IApplicationUserService {
    void processWithdrawalRequest(WithdrawalRequestDTO withdrawalRequestDto);

    void processOtherRequest(OtherApplicationDTO otherRequestDto);

    List<ApplicationUser> getApplicationsByType(Long applicationTypeId);

    String processWithdrawalPayment(Long applicationUserId);

    String approveApplication(Long applicationId, MultipartFile approvalImage) throws IOException;

    String rejectApplication(Long applicationId);

    String completeWithdrawalRequestWithApproval(Long applicationUserId, MultipartFile approvalImage) throws IOException;
    void cancelWithdrawalRequest(Long withdrawalRequestId);

    List<ApplicationUser> getApplicationUserByUserName();
    ApprovalRecord getApprovalRecordByApplicationUserId(Long applicationUserId);
}
