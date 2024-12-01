package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "violations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Violation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "violation_id")
    private Long violationId;

    @ManyToOne
    @JoinColumn(name = "teacher_name", referencedColumnName = "teacher_name", nullable = false)
    private Teacher teacher;

    @Column(name = "violation_count", nullable = false)
    private int violationCount;

    @Column(name = "penalty_percentage", nullable = false)
    private double penaltyPercentage;

    @Column(name = "last_violation_date")
    private LocalDateTime lastViolationDate;

    @Column(name = "description")
    private String description;
}
