package com.example.FPTLSPlatform.service.impl;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
@Service
public class OTPGmailService {
    private final Map<String, Integer> otpStorage = new HashMap<>();

    public int generateOTP(String email) {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        otpStorage.put(email, otp);
        return otp;
    }

    public Integer getOTP(String email) {
        return otpStorage.get(email);
    }

    public void clearOTP(String email) {
        otpStorage.remove(email);
    }
}
