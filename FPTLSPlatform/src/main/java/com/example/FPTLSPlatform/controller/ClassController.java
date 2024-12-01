package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.ClassDTO;
import com.example.FPTLSPlatform.dto.ResponseDTO;
import com.example.FPTLSPlatform.dto.StudentDTO;
import com.example.FPTLSPlatform.service.IClassService;
import com.example.FPTLSPlatform.service.IOrderService;
import com.example.FPTLSPlatform.service.impl.ClassService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/classes")
public class ClassController {

    private final IClassService classService;
    private final ObjectMapper objectMapper;
    private final IOrderService orderService;

    public ClassController(ClassService classService, ObjectMapper objectMapper, IOrderService orderService) {
        this.classService = classService;
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @PostMapping("")
    public ResponseEntity<?> createClass(@RequestPart("classDTO") String classDTOJson,
                                         @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            ClassDTO classDTO = objectMapper.readValue(classDTOJson, ClassDTO.class);

            ClassDTO createdClass = classService.createClass(classDTO, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdClass);
        } catch (RuntimeException | GeneralSecurityException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{classId}/students")
    @PreAuthorize("hasAnyAuthority('STAFF', 'STUDENT', 'TEACHER')")
    public ResponseEntity<Page<StudentDTO>> getAllStudentsInClass(
            @PathVariable Long classId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<StudentDTO> students = classService.getAllStudentsInClass(classId, PageRequest.of(page, size));
        return ResponseEntity.ok(students);
    }

    @PutMapping("/{classId}")
    public ResponseEntity<?> updateClass(@PathVariable Long classId,
                                         @RequestPart("classDTO") String classDTOJson,
                                         @RequestParam(value = "image", required = false) MultipartFile image) {
        try {
            ClassDTO classDTO = objectMapper.readValue(classDTOJson, ClassDTO.class);

            ClassDTO updatedClass = classService.updateClass(classId, classDTO, image);
            return ResponseEntity.status(HttpStatus.OK).body(updatedClass);
        } catch (RuntimeException | IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @PostMapping("/confirm-class")
    public ResponseEntity<ResponseDTO<ClassDTO>> confirmClassCompletion(
            @RequestParam Long classId) {
        try {

            ClassDTO classDTO = classService.confirmClassCompletion(classId, getCurrentUsername());
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

    @GetMapping("/getByClassId/{classId}")
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

    @GetMapping("/my-classes")
    public ResponseEntity<?> getAllClassesByCurrentTeacher() {
        try {
            List<ClassDTO> classList = classService.getAllClassesByCurrentTeacher();
            return ResponseEntity.status(HttpStatus.OK).body(classList);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/by-major")
    public ResponseEntity<List<ClassDTO>> getClassByMajor() {
        List<ClassDTO> classDTOList = classService.getClassByMajor();
        return ResponseEntity.ok(classDTOList);
    }

    @GetMapping("/StatusCompleted")
    public ResponseEntity<List<ClassDTO>> getCompletedClasses() {
        List<ClassDTO> completedClasses = classService.getClassesByStatusCompleted();
        return ResponseEntity.ok(completedClasses);
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @PostMapping("/cancel/{classId}")
    public ResponseEntity<String> cancelClass(@PathVariable Long classId) {
        try {
            String response = classService.cancelClass(classId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
