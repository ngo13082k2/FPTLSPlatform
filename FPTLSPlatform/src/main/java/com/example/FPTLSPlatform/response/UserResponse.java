package com.example.FPTLSPlatform.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserResponse {
    private String username;
    private String email;
    private String fullName;
    private String status;
    private String token;
    private String address;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String phoneNumber;
    public UserResponse(String username, String email, String fullName, String status, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    public UserResponse(String username, String email, String fullName, String status, String address, LocalDateTime createdDate, LocalDateTime modifiedDate, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.status = status;
        this.address = address;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.phoneNumber = phoneNumber;

    }


}
