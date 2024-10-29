package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Optional<Teacher> findByTeacherName(String teacherName);

    boolean existsByTeacherName(String teacherName);
    Optional<Teacher> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    Teacher getTeacherByTeacherName(String teacherName);
    long count();
}
