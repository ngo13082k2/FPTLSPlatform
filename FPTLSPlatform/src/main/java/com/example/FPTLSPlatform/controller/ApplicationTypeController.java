package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.exception.ApplicationTypeDTO;
import com.example.FPTLSPlatform.model.ApplicationType;
import com.example.FPTLSPlatform.service.impl.ApplicationTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/application-type")
public class ApplicationTypeController {

    @Autowired
    private ApplicationTypeService applicationTypeService;

    @PostMapping("")
    public ResponseEntity<ApplicationType> createApplicationType(@RequestBody ApplicationTypeDTO applicationTypeDto) {
        ApplicationType applicationType = applicationTypeService.createApplicationType(applicationTypeDto);
        return ResponseEntity.ok(applicationType);
    }
}
