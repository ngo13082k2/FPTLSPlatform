package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.request.VerifyOtpRequest;
import com.example.FPTLSPlatform.service.IForgotPasswordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ForgotPasswordService implements IForgotPasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;

    public ForgotPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder, OTPService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request, HttpSession session) {
        User user = userRepository.findByPhoneNumber(request.getPhoneNumber())
                .orElseThrow(() -> new RuntimeException("Phone number not found"));

        String otp = otpService.generateOtp(user.getPhoneNumber());
        otpService.sendOtpToPhone(user.getPhoneNumber(), otp);

        session.setAttribute("phoneNumber", user.getPhoneNumber());
    }
    @Override
    public void verifyOtp(VerifyOtpRequest request, HttpSession session) {
        String phoneNumber = (String) session.getAttribute("phoneNumber");
        if (phoneNumber == null) {
            throw new RuntimeException("Phone number session expired or not found");
        }

        boolean isOtpValid = otpService.validateOtp(phoneNumber, request.getOtp());
        if (!isOtpValid) {
            throw new RuntimeException("Invalid OTP");
        }

        session.setAttribute("otp", request.getOtp());
    }


    @Override
    public void resetPassword(ResetPasswordRequest request, HttpSession session) {
        String phoneNumber = (String) session.getAttribute("phoneNumber");
        String otp = (String) session.getAttribute("otp");

        if (phoneNumber == null || otp == null) {
            throw new RuntimeException("Session expired or information missing");
        }

        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Phone number not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        session.invalidate();
    }

}
