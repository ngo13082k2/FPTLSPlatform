package com.example.FPTLSPlatform.dto;

import jakarta.persistence.Column;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long applicationId;

    private String status;

    private String title;

    private String major;

    private String experience;

    private String cv;

    private String extraSkills;

    private String certificate;

    private String description;

    private String teacherName;

    private String rejectionReason;
}