package com.example.FPTLSPlatform.request;


import com.example.FPTLSPlatform.model.enums.Role;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequest {
    private String username;
    private String password;
    private String address;
    private String fullname;

}
