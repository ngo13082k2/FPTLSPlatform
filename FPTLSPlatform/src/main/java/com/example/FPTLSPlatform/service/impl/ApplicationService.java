package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.Application;
import com.example.FPTLSPlatform.repository.ApplicationRepository;
import com.example.FPTLSPlatform.service.IApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationService implements IApplicationService {

    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public ResponseEntity<?> createApplication(Application application) {
        applicationRepository.save(application);
        return ResponseEntity.ok().build();
    }
    public ResponseEntity<Application> updateApplication(Long id) {
        Optional<Application> optionalApplication = applicationRepository.findById(id);

        if (optionalApplication.isPresent()) {
            Application application = optionalApplication.get();

            application.setStatus("Approved");
            applicationRepository.save(application);

            return ResponseEntity.ok().body(application);
        }
        return ResponseEntity.notFound().build();
    }


}
