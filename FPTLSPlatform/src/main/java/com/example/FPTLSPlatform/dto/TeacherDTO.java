package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherDTO {
    private String phoneNumber;
    private String address;
    private String email;
    private String fullName;
    private String certificate;
    private String description;
    private String avatarImage;
    private Set<String> major;
}

