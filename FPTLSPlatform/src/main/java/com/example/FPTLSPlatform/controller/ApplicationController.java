package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.service.impl.ApplicationService;
import com.example.FPTLSPlatform.service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

            Application application = applicationService.updateApplication(id).getBody();
            if (application != null) {
                emailService.sendSimpleMessage(application.getTeacher().getTeacherName(),"Your application had approved", "SIUUUUUUUUU");

            }
            return applicationService.updateApplication(id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
