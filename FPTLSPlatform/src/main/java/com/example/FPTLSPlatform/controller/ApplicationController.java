package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.service.impl.ApplicationService;
import com.example.FPTLSPlatform.service.impl.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/applications")
public class ApplicationController {
    @Autowired
    private ApplicationService applicationService;
    @Autowired
    private EmailService emailService;

    @PostMapping("/approve_application")
    public ResponseEntity<?> approveApplication(@RequestParam Long id) {
        try {

            ApplicationDTO applicationDTO = applicationService.updateApplication(id);
            if (applicationDTO != null) {
                emailService.sendSimpleMessage(Objects.requireNonNull(applicationDTO).getTeacherName(),"Your application had approved", "SIUUUUUUUUU");
            }
            return ResponseEntity.ok(applicationDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/create-application")
    public ResponseEntity<?> createApplication(@RequestBody ApplicationDTO applicationDTO, HttpSession session) {
        try {
            ApplicationDTO application = applicationService.createApplication(applicationDTO, session);
            return ResponseEntity.ok(application);
        }catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Page<ApplicationDTO>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDTO> applications = applicationService.getAllApplications(pageable);
        return ResponseEntity.ok(applications);
    }


    @GetMapping("/staff")
    public ResponseEntity<Page<ApplicationDTO>> getApplicationsByStaff(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
) {
        String actualToken = token.substring(7);
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDTO> applications = applicationService.getApplicationsByStaff(actualToken, pageable);
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/assign/{applicationId}")
    public ResponseEntity<ApplicationDTO> assignApplication(@PathVariable Long applicationId,
                                                            @RequestParam String staffUsername) {
        Application application = applicationService.assignApplicationToStaff(applicationId, staffUsername);

        ApplicationDTO applicationDTO = ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .title(application.getTitle())
                .description(application.getDescription())
                .status(application.getStatus())
                .teacherName(application.getTeacher().getTeacherName())
                .build();

        return ResponseEntity.ok(applicationDTO);
    }
}
