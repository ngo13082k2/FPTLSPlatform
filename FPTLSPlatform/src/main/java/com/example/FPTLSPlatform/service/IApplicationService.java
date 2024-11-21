package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.model.Certificate;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IApplicationService {
    ApplicationDTO createApplication(ApplicationDTO applicationDTO, List<MultipartFile> certificate, List<String> certificateNames, HttpSession session) throws IOException;

    ApplicationDTO approveApplication(Long id) throws MessagingException;

    Page<ApplicationDTO> getAllApplications(Pageable pageable);

    Page<ApplicationDTO> getApplicationsByStaff(String staffUsername, Pageable pageable);

    Application assignApplicationToStaff(Long applicationId, String staffUsername);

    Page<ApplicationDTO> getPendingApplications(Pageable pageable);

    void assignApplicationsToAllStaff();

    ApplicationDTO rejectApplication(Long id, String rejectionReason);
}