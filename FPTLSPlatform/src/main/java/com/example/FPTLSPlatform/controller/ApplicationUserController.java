package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.model.ApplicationUser;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import com.example.FPTLSPlatform.service.impl.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/applicationUser")
public class ApplicationUserController {
    @Autowired
    private IApplicationUserService applicationUserService;

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
    public String completeWithdrawalRequest(@RequestParam Long applicationUserId) {
        return applicationUserService.completeWithdrawalRequest(applicationUserId);
    }

    @PutMapping("/approve")
    public String approveRequest(@RequestParam Long applicationUserId) {
        return applicationUserService.approveApplication(applicationUserId);
    }

    @PutMapping("/reject")
    public String rejectRequest(@RequestParam Long applicationUserId) {
        return applicationUserService.rejectApplication(applicationUserId);
    }
}
