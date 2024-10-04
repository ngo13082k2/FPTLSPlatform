package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.config.VNPayConfig;
import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.TransactionHistoryRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.repository.WalletRepository;
import com.example.FPTLSPlatform.service.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class VNPayService implements IVNPayService {

    @Value("${vnpay.tmn_code}")
    private String vnp_TmnCode;

    @Value("${vnpay.hash_secret}")
    private String vnp_HashSecret;

    @Value("${vnpay.api_url}")
    private String vnp_PayUrl;

    @Value("${vnpay.return_url}")
    private String vnp_ReturnUrl;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionHistoryRepository transactionHistoryRepository;
    public VNPayService(UserRepository userRepository, WalletRepository walletRepository, TransactionHistoryRepository transactionHistoryRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionHistoryRepository = transactionHistoryRepository;
    }

    public String generatePaymentUrl(Long amount, HttpServletRequest request) {
        try {
            String vnp_Version = "2.1.0";
            String vnp_Command = "pay";
            String vnp_OrderInfo = "Nap tien";
            String orderType = "other";

            String vnp_TxnRef = VNPayConfig.getRandomNumber(8);
            String vnp_IpAddr = VNPayConfig.getIpAddress(request);
            String locale = "vn";
            String currCode = "VND";

            Map<String, String> vnp_Params = new HashMap<>();
            vnp_Params.put("vnp_Version", vnp_Version);
            vnp_Params.put("vnp_Command", vnp_Command);
            vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
            vnp_Params.put("vnp_Amount", String.valueOf(amount * 100));
            vnp_Params.put("vnp_CurrCode", currCode);
            vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
            vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
            vnp_Params.put("vnp_OrderType", orderType);
            vnp_Params.put("vnp_Locale", locale);
            vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
            vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

            Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
            String vnp_CreateDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

            cld.add(Calendar.MINUTE, 15);
            String vnp_ExpireDate = formatter.format(cld.getTime());
            vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

            List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
            Collections.sort(fieldNames);

            StringBuilder hashData = new StringBuilder();
            StringBuilder query = new StringBuilder();

            for (String fieldName : fieldNames) {
                String fieldValue = vnp_Params.get(fieldName);
                if ((fieldValue != null) && (fieldValue.length() > 0)) {
                    hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()))
                            .append('=')
                            .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    query.append('&');
                    hashData.append('&');
                }
            }

            hashData.setLength(hashData.length() - 1);
            query.setLength(query.length() - 1);

            String secureHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());
            query.append("&vnp_SecureHash=").append(secureHash);

            return vnp_PayUrl + "?" + query.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String processVNPayReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, String[]> paramMap = request.getParameterMap();
        List<String> fieldNames = new ArrayList<>(paramMap.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        String vnpSecureHash = "";

        // Loại bỏ vnp_SecureHash và vnp_SecureHashType khi tính toán chữ ký
        for (String fieldName : fieldNames) {
            if (!"vnp_SecureHash".equals(fieldName) && !"vnp_SecureHashType".equals(fieldName)) {
                String[] fieldValueArr = paramMap.get(fieldName);
                String fieldValue = fieldValueArr[0];

                // Sử dụng mã hóa UTF-8 thay vì US-ASCII
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                hashData.append('&');
            } else if ("vnp_SecureHash".equals(fieldName)) {
                vnpSecureHash = paramMap.get(fieldName)[0];
            }
        }

        // Xóa ký tự '&' cuối cùng nếu có
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        // Tính toán chữ ký hash
        String calculatedHash = VNPayConfig.hmacSHA512(vnp_HashSecret, hashData.toString());

        // So sánh chữ ký
        if (vnpSecureHash.equalsIgnoreCase(calculatedHash)) {
            String amount = request.getParameter("vnp_Amount");
            String responseCode = request.getParameter("vnp_ResponseCode");

            if ("00".equals(responseCode)) {
                long amountInVND = Long.parseLong(amount) / 100;

                String username = getCurrentUsername();

                try {
                    updateWalletBalance(username, amountInVND);
                } catch (Exception e) {
                    return "Thanh toán không thành công do chưa nạp tiền vào ví: " + e.getMessage();
                }

                return "Thanh toán thành công! Số dư ví đã được cập nhật cho người dùng: " + username;


            } else {
                return "Giao dịch không thành công!";
            }
        } else {
            System.out.println("Chữ ký không hợp lệ! Dữ liệu tính toán: " + hashData.toString());
            System.out.println("Chữ ký tính toán: " + calculatedHash);
            System.out.println("Chữ ký VNPay trả về: " + vnpSecureHash);
            return "Chữ ký không hợp lệ!";
        }
    }





    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    public void updateWalletBalance(String username, long amount) throws Exception {
        System.out.println("Cập nhật ví cho người dùng: " + username + " với số tiền: " + amount);

        Optional<User> optionalUser = userRepository.findByUserName(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (user.getWallet() != null) {
                double currentBalance = user.getWallet().getBalance();

                System.out.println("Số dư hiện tại của ví: " + currentBalance);

                user.getWallet().setBalance(currentBalance + amount);

                walletRepository.save(user.getWallet());

                System.out.println("Số dư mới của ví: " + user.getWallet().getBalance());
                TransactionHistory transactionHistory = new TransactionHistory();
                transactionHistory.setAmount(amount);
                transactionHistory.setTransactionDate(LocalDateTime.now());
                transactionHistory.setUser(user);

                transactionHistoryRepository.save(transactionHistory);
            } else {
                throw new Exception("Người dùng không có ví.");
            }
        } else {
            throw new Exception("Không tìm thấy người dùng: " + username);
        }
    }



}
