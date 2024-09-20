package com.example.FPTLSPlatform.Controller;

import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.service.impl.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse response = authService.login(request);
        return ResponseEntity.ok(response);
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