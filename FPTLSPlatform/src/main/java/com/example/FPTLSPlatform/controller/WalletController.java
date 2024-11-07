package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.TransactionHistoryDTO;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.service.IWalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wallet")
public class WalletController {

    @Autowired
    private IWalletService walletService;

    @GetMapping("/balance")
    public ResponseEntity<?> getWalletBalance() {
        try {
            Wallet wallet = walletService.getWalletByUserName();
            Map<String, Object> response = new HashMap<>();
            response.put("balance", wallet.getBalance());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory() {
        try {
            List<TransactionHistoryDTO> history = walletService.getTransactionHistory();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }
    @PostMapping("/refund")
    public ResponseEntity<?> refundToWallet(@RequestBody Map<String, Long> request) {
        try {
            Long amount = request.get("amount");
            walletService.refundToWallet(amount);
            return ResponseEntity.ok("Hoàn tiền thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }
}
