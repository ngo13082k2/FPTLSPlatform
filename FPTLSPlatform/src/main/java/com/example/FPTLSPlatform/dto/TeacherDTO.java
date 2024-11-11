package com.example.FPTLSPlatform.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherDTO {
    private String phoneNumber;
    private String address;
    private String email;
    private String fullName;
    private String certificate;
    private String description;
}

