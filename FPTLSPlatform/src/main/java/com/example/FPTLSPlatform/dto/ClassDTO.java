package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    private ClassStatus status;

    private String location;

    private Integer maxStudents;

    private Long price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    private String teacherName;

    private LocalDate startDate;

    private String courseCode;

    private String fullName;

    private List<StudentDTO> students;

    private String imageUrl;

}
