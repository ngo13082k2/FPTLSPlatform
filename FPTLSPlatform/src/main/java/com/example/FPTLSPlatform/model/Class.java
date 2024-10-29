package com.example.FPTLSPlatform.model;

import com.example.FPTLSPlatform.model.enums.ClassStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "classes")
@NoArgsConstructor
@AllArgsConstructor
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ClassStatus status;

    @Column(name = "location")
    private String location;

    @Column(name = "max_students")
    private Integer maxStudents;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "image")
    private String image;

    @ManyToOne
    @JoinColumn(name = "teacher_name", referencedColumnName = "teacher_name", nullable = false)
    private Teacher teacher;

    @Column(name = "create_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Course courses;

    @Column(name = "day_of_week")
    private String dayOfWeek;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private Slot slot;

}
