package com.example.FPTLSPlatform.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long applicationId;

    private String status;

    private String title;

    private Set<Long> categoryIds;

    private Set<String> courseCodes;

    private String certificate;

    private String description;

    private String teacherName;

    private String rejectionReason;
}