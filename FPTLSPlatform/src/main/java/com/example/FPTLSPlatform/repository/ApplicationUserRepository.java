package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
}
