package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Course;
import com.example.FPTLSPlatform.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByCourse_CourseCode(String courseCode);
    List<Document> findByCourse(Course course);
    boolean existsByCourse_CourseCode(String courseCode);

}