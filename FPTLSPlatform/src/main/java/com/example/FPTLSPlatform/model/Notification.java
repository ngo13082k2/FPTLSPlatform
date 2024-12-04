package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "notification")
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String name;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String title;

    @Column(nullable = false, columnDefinition = "NVARCHAR(MAX)")
    private String description;

    private String type;

    private String username;

    private LocalDateTime createAt;

    @Column(nullable = false, columnDefinition = "BIT default 0")
    private boolean readStatus;
}
