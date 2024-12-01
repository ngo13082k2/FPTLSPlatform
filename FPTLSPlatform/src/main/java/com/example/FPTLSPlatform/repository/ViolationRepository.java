package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.Violation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViolationRepository extends JpaRepository<Violation, Long> {
    Violation findByTeacher(Teacher teacher);
}

