package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.TeacherDTO;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.service.IUserService;
import com.example.FPTLSPlatform.service.impl.AuthService;
import com.example.FPTLSPlatform.service.impl.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final IUserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    public AuthController(AuthService authService, IUserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register-student")
    public ResponseEntity<UserResponse> registerStudent(@RequestBody RegisterRequest request, HttpSession session) throws MessagingException {
        UserResponse userResponse = authService.register(request);

        session.setAttribute("email", request.getEmail());

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/confirm-otp")
    public ResponseEntity<Map<String, Object>> confirmOTP(@RequestParam int otp, HttpSession session) {
        UserResponse userResponse = authService.confirmOTP(otp);


        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng ký thành công");
        response.put("user", userResponse);

        return ResponseEntity.status(201).body(response);
    }

    //    @PostMapping("/register-teacher")
//    public ResponseEntity<String> registerTeacher(@RequestBody RegisterRequest request) {
//        authService.register(request, Role.TEACHER);
//        return ResponseEntity.ok("Teacher registered successfully. Status: Pending");
//    }
    @PostMapping("/register-teacher")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request, HttpSession session) {
        try {
            UserResponse response = authService.registerTeacher(request, session);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/byToken")
    public ResponseEntity<UserResponse> viewCurrentUser(@RequestHeader("Authorization") String token) {
        UserResponse userResponse = authService.viewCurrentUser(token);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping("")
    public ResponseEntity<UserResponse> updateCurrentUser(@RequestHeader("Authorization") String token, @RequestBody AuthenticationRequest request) {
        UserResponse updatedUser = authService.updateCurrentUser(token, request);
        return ResponseEntity.ok(updatedUser);
    }
    @PutMapping("/updateTeacher")
    public ResponseEntity<Teacher> updateLoggedInTeacher(
            @RequestParam("teacherDTO") String teacherDTOJson,
            @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage,
            @RequestParam(value = "avatarImage", required = false) MultipartFile avatarImage) {
        try {
            TeacherDTO teacherDTO = objectMapper.readValue(teacherDTOJson, TeacherDTO.class);

            Teacher updatedTeacher = authService.updateLoggedInTeacher(teacherDTO, backgroundImage, avatarImage);
            return ResponseEntity.ok(updatedTeacher);
        } catch (IOException e) {
            return ResponseEntity.status(500).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String username) {
        UserResponse userResponse = authService.getUserByUserName(username);
        return ResponseEntity.ok(userResponse);
    }
    @GetMapping("GetTeacher/{teacherName}")
    public ResponseEntity<TeacherDTO> getTeacherByTeacherName(@PathVariable String teacherName) {
        TeacherDTO teacherDTO = authService.getTeacherByTeacherName(teacherName);
        return ResponseEntity.ok(teacherDTO);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody Map<String, String> request) throws MessagingException {
        String email = request.get("email");
        String response = authService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/confirm-otpForgot")
    public ResponseEntity<String> confirmOtp(@RequestBody Map<String, Object> request) {
        int otp = (int) request.get("otp");
        String response = authService.confirmOtpForPasswordReset(otp);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        String response = authService.resetPassword(newPassword);
        return ResponseEntity.ok(response);
    }


}