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
    private String twilioPhoneNumber;  // Số Twilio của bạn, ví dụ +1 814 747 5403

    private final Map<String, String> otpStorage = new HashMap<>();

    // Tạo mã OTP ngẫu nhiên và lưu vào otpStorage
    public String generateOtp(String phonenumber) {
        String otp = String.valueOf((int) (Math.random() * 9000) + 1000);  // Tạo mã OTP ngẫu nhiên 4 chữ số
        otpStorage.put(phonenumber, otp);
        return otp;
    }

    // Gửi mã OTP tới số điện thoại sử dụng Twilio
    public void sendOtpToPhone(String phonenumber, String otp) {
        // Định dạng số điện thoại thành E.164 (ví dụ: +84988679157 cho số điện thoại Việt Nam)
        String e164PhoneNumber = formatPhoneNumberToE164(phonenumber);

        // Khởi tạo Twilio SDK khi cần gửi OTP
        Twilio.init(accountSid, authToken);

        // Gửi tin nhắn OTP qua Twilio, sử dụng số điện thoại đã được định dạng
        Message message = Message.creator(
                        new PhoneNumber(e164PhoneNumber),  // Số điện thoại người nhận theo định dạng E.164
                        new PhoneNumber(twilioPhoneNumber),  // Số điện thoại Twilio của bạn
                        "Your OTP code is: " + otp)  // Nội dung tin nhắn
                .create();

        System.out.println("Sent OTP " + otp + " to phone " + e164PhoneNumber + ". Message SID: " + message.getSid());
    }

    // Xác thực OTP
    public boolean validateOtp(String phonenumber, String otp) {
        String storedOtp = otpStorage.get(phonenumber);

        if (storedOtp == null) {
            // Nếu không tìm thấy OTP cho số điện thoại này, trả về lỗi hoặc false
            throw new RuntimeException("OTP not found or expired for this phone number.");
        }

        // Kiểm tra OTP có khớp không
        return storedOtp.equals(otp);
    }

    // Chuyển đổi số điện thoại sang định dạng E.164
    private String formatPhoneNumberToE164(String phonenumber) {
        // Nếu số điện thoại bắt đầu bằng 0 (dành cho số điện thoại Việt Nam), thay thế bằng +84
        if (phonenumber.startsWith("0")) {
            return "+84" + phonenumber.substring(1);  // Bỏ số 0 đầu và thêm mã quốc gia +84
        }
        // Nếu số điện thoại đã có định dạng quốc tế (bắt đầu bằng dấu +), giữ nguyên
        return phonenumber;
    }
}
