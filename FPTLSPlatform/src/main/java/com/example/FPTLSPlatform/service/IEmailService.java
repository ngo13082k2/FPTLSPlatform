package com.example.FPTLSPlatform.service;

public interface IEmailService {
    void sendSimpleMessage(
            String to, String subject, String text);
}
