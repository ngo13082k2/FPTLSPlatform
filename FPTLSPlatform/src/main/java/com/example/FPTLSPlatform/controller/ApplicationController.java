package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.exception.ApplicationAlreadyApprovedException;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.service.IApplicationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final IApplicationService applicationService;
    private final ObjectMapper objectMapper;

    public ApplicationController(IApplicationService applicationService,
                                 ObjectMapper objectMapper) {
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/approve_application")
    public ResponseEntity<?> approveApplication(@RequestParam Long id) {
        try {
            ApplicationDTO applicationDTO = applicationService.approveApplication(id);
            return ResponseEntity.ok(applicationDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ApplicationAlreadyApprovedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred." + e.getMessage());
        }
    }

    @PostMapping("/reject_application")
    public ResponseEntity<?> rejectApplication(@RequestParam Long id, @RequestBody String rejectionReason) {
        try {
            ApplicationDTO applicationDTO = applicationService.rejectApplication(id, rejectionReason);
            return ResponseEntity.ok(applicationDTO);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ApplicationAlreadyApprovedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred." + e.getMessage());
        }
    }


    @PostMapping("/create-application")
    public ResponseEntity<?> createApplication(
            @RequestParam("applicationDTO") String applicationDTOJson,
            @RequestPart("certificates") List<MultipartFile> certificateFiles,
            @RequestParam("certificateNames") List<String> certificateNames,
            HttpSession session) {
        try {
            ApplicationDTO applicationDTO = objectMapper.readValue(applicationDTOJson, ApplicationDTO.class);
            ApplicationDTO application = applicationService.createApplication(applicationDTO, certificateFiles, certificateNames, session);
            return ResponseEntity.ok(application);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ApplicationAlreadyApprovedException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    @GetMapping("/all")
    public ResponseEntity<Page<ApplicationDTO>> getAllApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDTO> applications = applicationService.getAllApplications(pageable);
        return ResponseEntity.ok().body(applications);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @GetMapping("/staff")
    public ResponseEntity<Page<ApplicationDTO>> getApplicationsByStaff(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        String staffUsername = getCurrentUsername();
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDTO> applications = applicationService.getApplicationsByStaff(staffUsername, pageable);
        return ResponseEntity.ok(applications);
    }

    @PostMapping("/assign/{applicationId}")
    public ResponseEntity<ApplicationDTO> assignApplication(@PathVariable Long applicationId) {

        String staffUsername = getCurrentUsername();
        Application application = applicationService.assignApplicationToStaff(applicationId, staffUsername);
        ApplicationDTO applicationDTO = ApplicationDTO.builder()
                .applicationId(application.getApplicationId())
                .description(application.getDescription())
                .status(application.getStatus())
                .teacherName(application.getTeacher().getTeacherName())
                .build();

        return ResponseEntity.ok().body(applicationDTO);
    }

    @GetMapping("/admin/applications/pending")
    public ResponseEntity<Page<ApplicationDTO>> getPendingApplications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ApplicationDTO> pendingApplications = applicationService.getPendingApplications(pageable);
        return ResponseEntity.ok(pendingApplications);
    }

    @PostMapping("/admin/applications/assign")
    public ResponseEntity<String> assignApplicationsToStaff() {
        try {
            applicationService.assignApplicationsToAllStaff();
            return ResponseEntity.ok("Applications assigned successfully to staff.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
