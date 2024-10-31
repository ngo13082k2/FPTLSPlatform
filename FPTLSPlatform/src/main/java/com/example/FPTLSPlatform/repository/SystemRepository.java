package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.System;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemRepository extends JpaRepository<System, Long> {
    System findByName(String name);
}
