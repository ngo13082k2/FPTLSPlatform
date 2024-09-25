package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.service.impl.ClassService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final ClassService classService;

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
}