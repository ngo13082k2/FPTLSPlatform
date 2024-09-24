package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Optional<Teacher> findByTeacherName(String teacherName);

    boolean existsByTeacherName(String teacherName);

    boolean existsByPhoneNumber(String phoneNumber);
}
