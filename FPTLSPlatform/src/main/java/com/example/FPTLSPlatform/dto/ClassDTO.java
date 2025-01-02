package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.enums.ClassStatus;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    private String creator;

    private String courseCode;

    private String fullName;

    private List<StudentDTO> students;

    private String imageUrl;

    private String imageTeacher;

    private List<DateSlotDTO> dateSlots;

    private List<DocumentDTO> documents;

}
