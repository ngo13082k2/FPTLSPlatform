package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentDTO {

    private String userName;

    private String phoneNumber;

    private String email;

    private String fullName;

    private String address;
}
