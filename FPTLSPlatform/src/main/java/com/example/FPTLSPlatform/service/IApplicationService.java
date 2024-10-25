package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.model.Application;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IApplicationService {
    ApplicationDTO createApplication(ApplicationDTO applicationDTO, HttpSession session);

    ApplicationDTO approveApplication(Long id) throws MessagingException;

    Page<ApplicationDTO> getAllApplications(Pageable pageable);

    Page<ApplicationDTO> getApplicationsByStaff(String staffUsername, Pageable pageable);

    Application assignApplicationToStaff(Long applicationId, String staffUsername);

    Page<ApplicationDTO> getPendingApplications(Pageable pageable);

    void assignApplicationsToAllStaff();

    ApplicationDTO rejectApplication(Long id, String rejectionReason);
}