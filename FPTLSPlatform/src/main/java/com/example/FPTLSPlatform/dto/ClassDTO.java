package com.example.FPTLSPlatform.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassDTO {
    private Long classId;
    private String name;
    private String code;
    private String description;
    private String status;
    private String location;
    private Integer maxStudents;
    private Long price;
    private LocalDateTime createDate;
    private String teacherName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String courseCode;
    private String fullName;
}
