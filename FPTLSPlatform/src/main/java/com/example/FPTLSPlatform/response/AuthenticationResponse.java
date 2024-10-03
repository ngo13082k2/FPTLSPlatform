package com.example.FPTLSPlatform.response;

import com.example.FPTLSPlatform.model.enums.Role;
import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private  String username;
    private  String email;
    private  String fullName;
    private  String status;
    private  String token;
    private Role role;
}
