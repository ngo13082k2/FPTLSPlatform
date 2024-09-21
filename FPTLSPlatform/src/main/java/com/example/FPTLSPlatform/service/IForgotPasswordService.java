package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.request.ForgotPasswordRequest;
import com.example.FPTLSPlatform.request.ResetPasswordRequest;

public interface IForgotPasswordService {
    void forgotPassword(ForgotPasswordRequest request);
    void  resetPassword(ResetPasswordRequest request);
}
