package com.example.FPTLSPlatform.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String fullname;
    private String phonenumber;
}