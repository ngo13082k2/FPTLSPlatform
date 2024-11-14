package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.CourseDTO;
import com.example.FPTLSPlatform.model.Course;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ICourseService {
    CourseDTO updateCourse(String courseCode, CourseDTO courseDTO, MultipartFile image) throws IOException;
    CourseDTO createCourse(CourseDTO courseDTO, MultipartFile image) throws IOException;
    void deleteCourse(String courseCode);
    List<CourseDTO> getAllCourses();
    List<CourseDTO> getCourseOfTeacher();
    long getTotalCourses();
}
