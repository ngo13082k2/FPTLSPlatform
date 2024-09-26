package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IApplicationService {
    ApplicationDTO createApplication(ApplicationDTO applicationDTO, HttpSession session);

    ApplicationDTO updateApplication(Long id);

    Page<ApplicationDTO> getAllApplications(Pageable pageable);


    Page<ApplicationDTO> getApplicationsByStaff(String staffUsername, Pageable pageable);
}