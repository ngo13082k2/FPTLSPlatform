package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@Entity
@Table(name = "feedback")
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @Column(name = "content_feedback", nullable = false)
    private String contentFeedback;

    @Column(nullable = false)
    private LocalDateTime timestamp;

}
