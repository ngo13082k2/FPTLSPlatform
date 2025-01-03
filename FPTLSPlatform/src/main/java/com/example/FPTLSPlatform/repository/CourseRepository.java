package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CourseRepository extends JpaRepository<Course, String> {
    List<Course> findByCategoriesCategoryIdIn(Set<Long> categoryIds);
    Optional<Course> findByCourseCode(String courseCode);
    boolean existsByCourseCode(String courseCode);


}
