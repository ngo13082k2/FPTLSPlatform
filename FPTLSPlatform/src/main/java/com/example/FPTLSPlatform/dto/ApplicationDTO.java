package com.example.FPTLSPlatform.dto;

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

    private String description;

    private String teacherName;
}
