package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {
    List<ApplicationUser> findByApplicationType_Id(Long applicationTypeId);


}
