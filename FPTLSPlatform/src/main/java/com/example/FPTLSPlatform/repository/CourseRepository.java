package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, String> {
}
