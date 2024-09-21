package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.request.VerifyOtpRequest;
import com.example.FPTLSPlatform.service.IForgotPasswordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/forgotpassword")
public class ForgotPasswordController {
    private final IForgotPasswordService iForgotPasswordService;

    public ForgotPasswordController(IForgotPasswordService iForgotPasswordService) {
        this.iForgotPasswordService = iForgotPasswordService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpSession session) {
        iForgotPasswordService.forgotPassword(request, session);
        return ResponseEntity.ok("OTP has been sent to your phone number");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtpRequest request, HttpSession session) {
        iForgotPasswordService.verifyOtp(request, session);
        return ResponseEntity.ok("OTP is valid");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request, HttpSession session) {
        iForgotPasswordService.resetPassword(request, session);
        return ResponseEntity.ok("Password has been reset successfully");
    }
}


