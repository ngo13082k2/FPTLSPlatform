package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
@Builder
@Entity
@Table(name = "teachers ")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {

    @Id
    @Column(name = "teacher_name", nullable = false, unique = true)
    private String teacherName;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "major")
    private String major;

    @Column(name = "address")
    private String address;

    @Column(name = "fullname", nullable = false)
    private String fullName;

    @Column(name = "status")
    private String status;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "wallet_id", referencedColumnName = "wallet_id")
    private Wallet wallet;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "application_id", referencedColumnName = "application_id")
    private Application application;
}
