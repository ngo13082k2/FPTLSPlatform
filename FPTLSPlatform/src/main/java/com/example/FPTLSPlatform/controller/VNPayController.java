package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.service.IVNPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RequestMapping("/payment")
@RestController
public class VNPayController {

    private final IVNPayService vnpayService;

    public VNPayController(IVNPayService vnpayService) {
        this.vnpayService = vnpayService;
    }


    @PostMapping("/recharge")
    public Map<String, String> createPayment(@RequestBody Map<String, Long> request, HttpServletRequest httpRequest) {
        Long amount = request.get("amount");
        String paymentUrl = vnpayService.generatePaymentUrl(amount, httpRequest);


        return Map.of("paymentUrl", paymentUrl);
    }

    @GetMapping("/return")
    public String handleVNPayReturn(HttpServletRequest request) throws UnsupportedEncodingException {
        return vnpayService.processVNPayReturn(request);
    }
}
