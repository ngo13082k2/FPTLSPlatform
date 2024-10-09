package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.ApplicationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationTypeRepository extends JpaRepository<ApplicationType, Long> {
    Optional<ApplicationType> findByName(String name);
}
