package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@Entity
@Table(name = "application_type")
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "application_type_id")
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
