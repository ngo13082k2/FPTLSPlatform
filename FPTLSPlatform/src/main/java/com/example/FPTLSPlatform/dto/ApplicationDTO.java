package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.Certificate;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationDTO {
    private Long applicationId;

    private String status;

    private List<CertificateDTO> certificate;

    private String description;

    private String teacherName;

    private String assignedStaff;

    private String rejectionReason;

}