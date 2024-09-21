package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.service.IForgotPasswordService;
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
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByPhonenumber(request.getPhonenumber())
                .orElseThrow(() -> new RuntimeException("Phone number not found"));

        // Tạo và gửi mã OTP tới số điện thoại của người dùng
        String otp = otpService.generateOtp(user.getPhonenumber());
        otpService.sendOtpToPhone(user.getPhonenumber(), otp);
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByPhonenumber(request.getPhonenumber())
                .orElseThrow(() -> new RuntimeException("Phone number not found"));

        // Xác minh mã OTP
        boolean isOtpValid = otpService.validateOtp(user.getPhonenumber(), request.getOtp());
        if (!isOtpValid) {
            throw new RuntimeException("Invalid OTP");
        }

        // Nếu OTP hợp lệ, cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
