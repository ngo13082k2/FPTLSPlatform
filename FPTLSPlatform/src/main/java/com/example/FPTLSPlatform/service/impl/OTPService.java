package com.example.FPTLSPlatform.service.impl;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OTPService {

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

    private final Map<String, String> otpStorage = new HashMap<>();

    public String generateOtp(String phoneNumber) {
        String otp = String.valueOf((int) (Math.random() * 9000) + 1000);
        otpStorage.put(phoneNumber, otp);
        return otp;
    }

    public void sendOtpToPhone(String phoneNumber, String otp) {
        String e164PhoneNumber = formatPhoneNumberTo84(phoneNumber);

        Twilio.init(accountSid, authToken);

        Message message = Message.creator(
                        new PhoneNumber(e164PhoneNumber),
                        new PhoneNumber(twilioPhoneNumber),
                        "Your OTP code is: " + otp)
                .create();

        System.out.println("Sent OTP " + otp + " to phone " + e164PhoneNumber + ". Message SID: " + message.getSid());
    }

    public boolean validateOtp(String phoneNumber, String otp) {
        String storedOtp = otpStorage.get(phoneNumber);

        if (storedOtp == null) {
            throw new RuntimeException("OTP not found or expired for this phone number.");
        }

        return storedOtp.equals(otp);
    }


    private String formatPhoneNumberTo84(String phoneNumber) {
        if (phoneNumber.startsWith("0")) {
            return "+84" + phoneNumber.substring(1);
        }
        return phoneNumber;
    }
}
