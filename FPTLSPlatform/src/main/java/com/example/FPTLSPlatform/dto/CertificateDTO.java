package com.example.FPTLSPlatform.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;

    private String name;

    private String fileUrl;
}
