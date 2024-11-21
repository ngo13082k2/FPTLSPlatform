package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
}
