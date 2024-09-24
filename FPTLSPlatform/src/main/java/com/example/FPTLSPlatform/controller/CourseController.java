package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.CourseDTO;
import com.example.FPTLSPlatform.service.impl.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;
    private final ObjectMapper objectMapper;

    @Autowired
    public CourseController(CourseService courseService, ObjectMapper objectMapper) {
        this.courseService = courseService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createCourse(
            @RequestParam("courseDTO") String courseJson,
            @RequestParam("image") MultipartFile image) {
        try {
            CourseDTO courseDTO = objectMapper.readValue(courseJson, CourseDTO.class);

            CourseDTO createdCourse = courseService.createCourse(courseDTO, image);
            return ResponseEntity.status(201).body("Course created successfully: " + objectMapper.writeValueAsString(createdCourse));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error creating course: " + e.getMessage());
        }
    }

    @PutMapping("/update/{courseCode}")
    public ResponseEntity<?> updateCourse(
            @PathVariable String courseCode,
            @RequestParam("courseDTO") String courseJson,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            CourseDTO courseDTO = objectMapper.readValue(courseJson, CourseDTO.class);

            CourseDTO updatedCourse = courseService.updateCourse(courseCode, courseDTO, image);
            return ResponseEntity.status(200).body("Course updated successfully: " + objectMapper.writeValueAsString(updatedCourse));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error updating course: " + e.getMessage());
        }
    }
}


