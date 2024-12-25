package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.CourseDTO;
import com.example.FPTLSPlatform.service.ICourseService;
import com.example.FPTLSPlatform.service.impl.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final ICourseService courseService;
    private final ObjectMapper objectMapper;
    @Autowired
    private HttpSession session;
    @Autowired
    public CourseController(ICourseService courseService, ObjectMapper objectMapper) {
        this.courseService = courseService;
        this.objectMapper = objectMapper;
    }
    @GetMapping("/courses")
    public ResponseEntity<List<CourseDTO>> getCourseOfTeacher() {
        List<CourseDTO> courses = courseService.getCourseOfTeacher();
        return ResponseEntity.ok(courses);
    }
    @PostMapping("")
    public ResponseEntity<?> createCourse(
            @RequestPart("courseDTO") String courseJson,
            @RequestPart("image") MultipartFile image) {
        try {
            CourseDTO courseDTO = objectMapper.readValue(courseJson, CourseDTO.class);

            CourseDTO createdCourse = courseService.createCourse(courseDTO, image);
            session.setAttribute("course_code", createdCourse.getCourseCode());

            return ResponseEntity.status(201).body("Course created successfully: " + objectMapper.writeValueAsString(createdCourse));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error creating course: " + e.getMessage());
        }
    }

    @PutMapping("/{courseCode}")
    public ResponseEntity<?> updateCourse(
            @PathVariable String courseCode,
            @RequestPart("courseDTO") String courseJson,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            CourseDTO courseDTO = objectMapper.readValue(courseJson, CourseDTO.class);

            CourseDTO updatedCourse = courseService.updateCourse(courseCode, courseDTO, image);
            return ResponseEntity.status(200).body("Course updated successfully: " + objectMapper.writeValueAsString(updatedCourse));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error updating course: " + e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllCourses() {
        try {
            List<CourseDTO> courses = courseService.getAllCourses();
            return ResponseEntity.ok(courses);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @DeleteMapping("/{courseCode}")
    public ResponseEntity<String> deleteCourse(@PathVariable String courseCode) {
        try {
            courseService.deleteCourse(courseCode);
            return ResponseEntity.status(HttpStatus.OK).body("Course deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}


