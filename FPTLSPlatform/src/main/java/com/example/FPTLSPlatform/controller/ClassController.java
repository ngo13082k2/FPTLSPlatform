package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import com.example.FPTLSPlatform.service.IClassService;
import com.example.FPTLSPlatform.service.impl.ClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final IClassService classService;

    public ClassController(ClassService classService) {
        this.classService = classService;
    }

    @PostMapping("")
    public ResponseEntity<?> createClass(@RequestBody ClassDTO classDTO) {
        try {
            ClassDTO createdClass = classService.createClass(classDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{classId}")
    public ResponseEntity<?> updateClass(@PathVariable Long classId, @RequestBody ClassDTO classDTO) {
        try {
            ClassDTO updatedClass = classService.updateClass(classId, classDTO);
            return ResponseEntity.status(HttpStatus.OK).body(updatedClass);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/confirm-class")
    public ResponseEntity<ResponseDTO<ClassDTO>> confirmClassCompletion(
            @RequestParam Long classId,
            @RequestParam String teacherUsername) {

        try {
            ClassDTO classDTO = classService.confirmClassCompletion(classId, teacherUsername);
            return ResponseEntity.ok(new ResponseDTO<>("SUCCESS", "Class has been confirmed as completed.", classDTO));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseDTO<>("ERROR", e.getMessage(), null));
        }
    }

    @GetMapping("/byCourse/{courseCode}")
    public ResponseEntity<?> getClassesByCourse(@PathVariable String courseCode) {
        try {
            List<ClassDTO> classes = classService.getClassesByCourse(courseCode);
            return ResponseEntity.ok(classes);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/{classId}")
    public ResponseEntity<?> getClassById(@PathVariable Long classId) {
        try {
            ClassDTO clazz = classService.getClassById(classId);
            return ResponseEntity.ok(clazz);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping
    public ResponseEntity<?> getAllClasses() {
        try {
            List<ClassDTO> classes = classService.getAllClasses();
            return ResponseEntity.ok(classes);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    @GetMapping("/teacher/{teacherName}")
    public ResponseEntity<?> getClassesByTeacherName(@PathVariable String teacherName) {
        try {
            List<ClassDTO> classes = classService.getClassesByTeacherName(teacherName);
            return ResponseEntity.ok(classes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
