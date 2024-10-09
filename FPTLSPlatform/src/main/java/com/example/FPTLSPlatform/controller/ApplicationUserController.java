package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.OtherApplicationDTO;
import com.example.FPTLSPlatform.dto.WithdrawalRequestDTO;
import com.example.FPTLSPlatform.service.IApplicationUserService;
import com.example.FPTLSPlatform.service.impl.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/applicationUser")
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
}
