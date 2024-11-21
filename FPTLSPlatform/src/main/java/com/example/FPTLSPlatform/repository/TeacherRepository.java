package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherRepository extends JpaRepository<Teacher, String> {
    Optional<Teacher> findByTeacherName(String teacherName);

    boolean existsByTeacherName(String teacherName);
    Optional<Teacher> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    Teacher getTeacherByTeacherName(String teacherName);
    long count();
    List<Teacher> findByStatus(String status);
    boolean existsByTeacherNameAndStatus(String teacherName, String status);
    boolean existsByPhoneNumberAndStatus(String phoneNumber, String status);
    boolean existsByEmailAndStatus(String email, String status);;

}
