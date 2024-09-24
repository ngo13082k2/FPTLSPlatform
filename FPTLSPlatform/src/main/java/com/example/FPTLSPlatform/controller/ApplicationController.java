package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ApplicationDTO;
import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.service.impl.ApplicationService;
import com.example.FPTLSPlatform.service.impl.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<ApplicationDTO> approveApplication(@RequestParam Long id) {
        try {

            ResponseEntity<ApplicationDTO> applicationDTO = applicationService.updateApplication(id);
            if (applicationDTO != null) {
                emailService.sendSimpleMessage(Objects.requireNonNull(applicationDTO.getBody()).getTeacherName(),"Your application had approved", "SIUUUUUUUUU");
            }
            return applicationDTO;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @PostMapping("/create-application")
    public ResponseEntity<?> createApplication(@RequestBody ApplicationDTO applicationDTO, HttpSession session) {
        try {
            return applicationService.createApplication(applicationDTO, session);
        }catch (Exception e) {
           return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
