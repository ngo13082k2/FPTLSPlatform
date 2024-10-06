package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface IVNPayService {
    String generatePaymentUrl(Long amount, HttpServletRequest request);

    String processVNPayReturn(HttpServletRequest request) throws UnsupportedEncodingException;

    void updateWalletBalance(String username, long amount) throws ResourceNotFoundException;
}