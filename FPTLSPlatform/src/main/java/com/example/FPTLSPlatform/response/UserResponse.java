package com.example.FPTLSPlatform.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String username;
    private String email;
    private String fullname;
    private String status;
    private String token; // Optional, only for login

    public UserResponse(String username, String email, String fullname, String status) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.status = status;
    }

    public UserResponse(String username, String email, String fullname, String status, String token) {
        this(username, email, fullname, status);
        this.token = token;
    }


}
