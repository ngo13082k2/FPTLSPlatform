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
    private String phonenumber;
    public UserResponse(String username, String email, String fullname, String status, String phonenumber) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.status = status;
        this.phonenumber = phonenumber;
    }

    public UserResponse(String username, String email, String fullname, String status, String address, LocalDateTime createdDate, LocalDateTime modifiedDate, String phonenumber) {
        this.username = username;
        this.email = email;
        this.fullname = fullname;
        this.status = status;
        this.address = address;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.phonenumber = phonenumber;

    }


}
