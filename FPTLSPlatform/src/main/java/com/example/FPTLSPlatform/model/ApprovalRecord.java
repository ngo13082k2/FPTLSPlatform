package com.example.FPTLSPlatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Builder
@Getter
@Setter
@Entity
@Table(name = "approval_record")
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "approval_record_id")
    private Long approvalRecordId;

    @OneToOne
    @JoinColumn(name = "application_user_id", nullable = false)
    private ApplicationUser applicationUser;

    @Column(name = "approved_by", nullable = false)
    private String approvedBy;

    @Column(name = "approval_image", nullable = false)
    private String approvalImage; // Đường dẫn hình ảnh xác thực

    @Column(name = "approval_date", nullable = false)
    private LocalDateTime approvalDate;
}
