package com.example.FPTLSPlatform.service;

import jakarta.mail.MessagingException;
import org.thymeleaf.context.Context;

public interface IEmailService {
    void sendSimpleMessage(String to, String subject, String text);

    void sendEmail(String to, String subject, String templateName, Context context);
    void sendVerificationCode(String toEmail, String verificationCode);
    void sendOTP(String toEmail, int otp) throws MessagingException;

}
