package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.ApplicationUser;
import com.example.FPTLSPlatform.request.ApplicationUserIdRequest;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import com.example.FPTLSPlatform.service.impl.ApplicationUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/applicationUser")
public class ApplicationUserController {
    @Autowired
    private IApplicationUserService applicationUserService;
    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/submit/withdrawal")
    public ResponseEntity<String> submitWithdrawal(@RequestBody WithdrawalRequestDTO withdrawalRequest) {
        applicationUserService.processWithdrawalRequest(withdrawalRequest);
        return ResponseEntity.ok("Withdrawal request submitted successfully");
    }

    @PostMapping("/submit/other")
    public ResponseEntity<String> submitOther(@RequestBody OtherApplicationDTO otherRequest) {
        applicationUserService.processOtherRequest(otherRequest);
        return ResponseEntity.ok("Other request submitted successfully");
    }

    @GetMapping("/applications/{applicationTypeId}")
    public ResponseEntity<List<ApplicationUser>> getApplicationsByType(@PathVariable Long applicationTypeId) {
        List<ApplicationUser> applications = applicationUserService.getApplicationsByType(applicationTypeId);
        return ResponseEntity.ok(applications);
    }

    //    @PostMapping("/process-withdrawal/{applicationUserId}")
//    public ResponseEntity<String> processWithdrawalPayment(@PathVariable Long applicationUserId) {
//        String response = applicationUserService.processWithdrawalPayment(applicationUserId);
//        return ResponseEntity.ok(response);
//    }
    @PostMapping("/complete")
    public ResponseEntity<String> completeWithdrawalRequest(@RequestParam String applicationUserJson,
                                                            @RequestParam(required = false) MultipartFile approvalImage) throws IOException {
        try {
            ApplicationUserIdRequest request = objectMapper.readValue(applicationUserJson, ApplicationUserIdRequest.class);

            // Gọi service xử lý
            String result = applicationUserService.completeWithdrawalRequestWithApproval(request.getApplicationUserId(), approvalImage);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }


    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable Long id,
                                                 @RequestParam(required = false) MultipartFile approvalImage) {
        try {
            // Gọi service xử lý phê duyệt yêu cầu
            String result = applicationUserService.approveApplication(id, approvalImage);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }
    @PutMapping("/reject/{id}")
    public String rejectRequest(@PathVariable Long id) {
        return applicationUserService.rejectApplication(id);
    }

    @PutMapping("/cancelWithdrawal/{id}")
    public ResponseEntity<String> cancelWithdrawal(@PathVariable Long id) {
        try {
            applicationUserService.cancelWithdrawalRequest(id);
            return ResponseEntity.ok("Withdrawal request canceled successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("getApplicationUserByUserName")
    public ResponseEntity<List<ApplicationUser>> getApplicationUserByUserName() {
        try {
            List<ApplicationUser> applicationUsers = applicationUserService.getApplicationUserByUserName();
            return ResponseEntity.ok(applicationUsers);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}
