package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@Entity
@Table(name = "application_user")
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_user_id")
    private Long applicationUserId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "application_type_id", nullable = false)
    private ApplicationType applicationType;
    @ManyToOne
    @JoinColumn(name = "user_name", nullable = false)
    private User user;
}
