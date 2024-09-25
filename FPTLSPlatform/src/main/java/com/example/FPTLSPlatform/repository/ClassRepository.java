package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Class;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassRepository extends JpaRepository<Class,Long> {
    List<Class> findByCoursesCourseCode(String courseCode);
    Optional<Class> findById(Long classId);

}
