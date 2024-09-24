package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.request.VerifyOtpRequest;
import com.example.FPTLSPlatform.service.IForgotPasswordService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForgotPasswordService implements IForgotPasswordService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPService otpService;
    private final TeacherRepository teacherRepository;
    public ForgotPasswordService(UserRepository userRepository, PasswordEncoder passwordEncoder, OTPService otpService, TeacherRepository teacherRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpService = otpService;
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request, HttpSession session) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(request.getPhoneNumber());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String otp = otpService.generateOtp(user.getPhoneNumber());
            otpService.sendOtpToPhone(user.getPhoneNumber(), otp);

            session.setAttribute("phoneNumber", user.getPhoneNumber());
        } else {
            Optional<Teacher> optionalTeacher = teacherRepository.findByPhoneNumber(request.getPhoneNumber());

            if (optionalTeacher.isPresent()) {
                Teacher teacher = optionalTeacher.get();
                String otp = otpService.generateOtp(teacher.getPhoneNumber());
                otpService.sendOtpToPhone(teacher.getPhoneNumber(), otp);

                session.setAttribute("phoneNumber", teacher.getPhoneNumber());
            } else {
                throw new RuntimeException("Phone number not found for User or Teacher");
            }
        }
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

        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        } else {
            Optional<Teacher> optionalTeacher = teacherRepository.findByPhoneNumber(phoneNumber);

            if (optionalTeacher.isPresent()) {
                Teacher teacher = optionalTeacher.get();
                teacher.setPassword(passwordEncoder.encode(request.getNewPassword()));
                teacherRepository.save(teacher);
            } else {
                throw new RuntimeException("Phone number not found for User or Teacher");
            }
        }

        session.invalidate();
    }

}
