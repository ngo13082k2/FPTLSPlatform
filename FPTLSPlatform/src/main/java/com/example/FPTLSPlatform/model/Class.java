package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Builder
@Getter
@Setter
@Entity
@Table(name = "class")
@NoArgsConstructor
@AllArgsConstructor
public class Class {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "class_id")
    private Long classId;

    @Column(nullable = false)
    private String name;

    private String code;
    private String description;

    private String status;
    private String location;

    @Column(name = "max_students")
    private int maxStudents;

    @Column(name = "enrolled_students")
    private int enrolledStudents;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @ManyToMany
    @JoinTable(
            name = "user_class",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users;

    @ManyToMany
    @JoinTable(
            name = "order_class",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private Set<Order> orders;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
}
