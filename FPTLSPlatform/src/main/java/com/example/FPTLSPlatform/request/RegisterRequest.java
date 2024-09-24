package com.example.FPTLSPlatform.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String phoneNumber;
    private LocalDateTime createdDate;
}