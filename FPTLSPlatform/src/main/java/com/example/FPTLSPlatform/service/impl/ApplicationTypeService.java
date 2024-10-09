package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.dto.ApplicationTypeDto;
import com.example.FPTLSPlatform.model.ApplicationType;
import com.example.FPTLSPlatform.repository.ApplicationTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationTypeService {

    @Autowired
    private ApplicationTypeRepository applicationTypeRepository;

    public ApplicationType createApplicationType(ApplicationTypeDto applicationTypeDto) {
        ApplicationType applicationType = ApplicationType.builder()
                .name(applicationTypeDto.getName())
                .build();
        return applicationTypeRepository.save(applicationType);
    }
}
