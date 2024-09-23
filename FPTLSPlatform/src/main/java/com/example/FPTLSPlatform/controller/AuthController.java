package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.service.impl.AuthService;
import com.example.FPTLSPlatform.service.impl.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register-student")
    public ResponseEntity<String> registerStudent(@RequestBody RegisterRequest request) {
        authService.register(request, Role.STUDENT);
        return ResponseEntity.ok("Student registered successfully");
    }
    @PostMapping("/register-teacher")
    public ResponseEntity<String> registerTeacher(@RequestBody RegisterRequest request) {
        authService.register(request, Role.TEACHER);
        return ResponseEntity.ok("Teacher registered successfully. Status: Pending");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            AuthenticationResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
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
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse userResponse = authService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }
}