package com.example.FPTLSPlatform.Controller;

import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.service.IForgotPasswordService;
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
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        iForgotPasswordService.forgotPassword(request);
        return ResponseEntity.ok("OTP has been sent to your phone number");
    }
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        iForgotPasswordService.resetPassword(request);
        return ResponseEntity.ok("Password has been reset successfully");
    }
}
