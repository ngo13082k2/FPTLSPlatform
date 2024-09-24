package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;

public interface IApplicationService {
    ResponseEntity<?> createApplication(ApplicationDTO applicationDTO, HttpSession session);

    ResponseEntity<ApplicationDTO> updateApplication(Long id);
}
