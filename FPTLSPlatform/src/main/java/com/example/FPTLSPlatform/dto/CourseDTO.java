package com.example.FPTLSPlatform.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDTO {
    private String courseCode;

    private String name;

    private String description;

    private String status;

    private String image;
    
//    private int duration;

    private Long categoryId;

    private String categoryName;
}