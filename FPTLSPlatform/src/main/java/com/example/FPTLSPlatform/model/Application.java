package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@Table(name = "applications")
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_id")
    private Long applicationId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "major")
    private String major;

    @Column(name = "experience")
    private String experience;

    @Column(name = "cv")
    private String cv;

    @Column(name = "extra_skills")
    private String extraSkills;

    @Column(name = "certificate")
    private String certificate;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_name", referencedColumnName = "teacher_name")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id", referencedColumnName = "user_name")
    private User assignedStaff;
}
