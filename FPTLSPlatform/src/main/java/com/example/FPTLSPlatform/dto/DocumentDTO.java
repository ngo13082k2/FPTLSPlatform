package com.example.FPTLSPlatform.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {
    private Long id;
    private String title;
    private String content;
    private String courseCode;
    private String filePath;
    private int completedSlots;

}
