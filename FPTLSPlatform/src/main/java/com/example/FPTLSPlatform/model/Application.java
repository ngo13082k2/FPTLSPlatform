package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @Column(name = "description")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Certificate> certificates = new ArrayList<>();

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "teacher_name", referencedColumnName = "teacher_name")
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(name = "assigned_staff_id", referencedColumnName = "user_name")
    private User assignedStaff;
}
