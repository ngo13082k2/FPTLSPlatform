package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.service.IVNPayService;
import com.example.FPTLSPlatform.service.impl.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
@RequestMapping("/payment")

@RestController
public class VNPayController {

    private IVNPayService vnpayService;


    @PostMapping("/recharge")
    public Map<String, String> createPayment(@RequestBody Map<String, Long> request, HttpServletRequest httpRequest) {
        Long amount = request.get("amount");
        String paymentUrl = vnpayService.generatePaymentUrl(amount, httpRequest);


        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/return")
    public String handleVNPayReturn(HttpServletRequest request) {
        return vnpayService.processVNPayReturn(request);
    }
}
