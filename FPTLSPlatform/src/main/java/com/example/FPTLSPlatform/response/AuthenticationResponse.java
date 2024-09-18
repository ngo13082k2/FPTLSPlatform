package com.example.FPTLSPlatform.response;

import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Setter
@Getter
@AllArgsConstructor
public class AuthenticationResponse {
    private  String username;
    private  String email;
    private  String fullname;
    private  String status;
    private  String token;
}
