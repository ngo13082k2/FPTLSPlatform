package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.Certificate;
import jakarta.validation.constraints.Size;
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

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String teacherName;

    private String assignedStaff;

    private String rejectionReason;

}