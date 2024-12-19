package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.util.List;
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
    private List<CertificateDTO> certificate;
    private String description;
    private String avatarImage;
    private Set<String> major;
    private int violation;
}

