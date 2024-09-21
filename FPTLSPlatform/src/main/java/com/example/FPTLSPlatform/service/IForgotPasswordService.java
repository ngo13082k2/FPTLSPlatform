package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;
import com.example.FPTLSPlatform.request.VerifyOtpRequest;
import jakarta.servlet.http.HttpSession;

public interface IForgotPasswordService {
     void forgotPassword(ForgotPasswordRequest request, HttpSession session)  ;   void resetPassword(ResetPasswordRequest request, HttpSession session);
     void verifyOtp(VerifyOtpRequest request, HttpSession sessio);
}