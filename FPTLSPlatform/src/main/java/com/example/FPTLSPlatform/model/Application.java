package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

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

    @ElementCollection
    @CollectionTable(name = "application_category_ids", joinColumns = @JoinColumn(name = "application_id"))
    private Set<Long> categoriesId;

    @ElementCollection
    @CollectionTable(name = "application_course_codes", joinColumns = @JoinColumn(name = "application_id"))
    private Set<String> courses;

    @Column(name = "certificate")
    private String certificate;

    private String rejectionReason;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_name", referencedColumnName = "teacher_name")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id", referencedColumnName = "user_name")
    private User assignedStaff;
}
