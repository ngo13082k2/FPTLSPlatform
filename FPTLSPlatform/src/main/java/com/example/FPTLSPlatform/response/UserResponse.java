package com.example.FPTLSPlatform.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UserResponse {
    private String username;
    private String email;
    private String fullname;
    private String status;
    private String token;
    private String address;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    public UserResponse(String username, String email, String fullname, String status) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.status = status;
    }

    public UserResponse(String username, String email, String fullname, String status, String address, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.status = status;
        this.address = address;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;

    }


}
