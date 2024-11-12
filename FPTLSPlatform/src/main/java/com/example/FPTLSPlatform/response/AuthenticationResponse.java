package com.example.FPTLSPlatform.response;

import com.example.FPTLSPlatform.model.Category;
import com.example.FPTLSPlatform.model.enums.Role;
import lombok.*;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

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
    private String phoneNumber;
    private String address;
    private Set<Category> major;
}
