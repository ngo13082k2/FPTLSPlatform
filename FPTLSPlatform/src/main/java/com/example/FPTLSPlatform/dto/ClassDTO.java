package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.Class;
import com.example.FPTLSPlatform.model.Slot;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Size;
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

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private ClassStatus status;

    private String location;

    private Integer maxStudents;

    private Long price;

    private LocalDateTime createDate;

    private String teacherName;

    private LocalDate startDate;

    private String courseCode;

    private String fullName;

    private List<StudentDTO> students;

    private String imageUrl;

    private Long slotId;

    private LocalDate endDate;

    private String dayOfWeek;

    private String imageTeacher;
}
