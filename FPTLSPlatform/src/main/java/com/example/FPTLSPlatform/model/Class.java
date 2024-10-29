package com.example.FPTLSPlatform.model;

import com.example.FPTLSPlatform.model.enums.ClassStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

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
    private LocalDateTime createDate;

    @Column(name = "start_date")
    private LocalDate startDate;

    @ManyToOne
    @JoinColumn(name = "course_code", referencedColumnName = "course_code", nullable = false)
    private Course courses;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

}
