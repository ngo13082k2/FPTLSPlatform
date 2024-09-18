package com.example.FPTLSPlatform.service.impl;



import com.example.FPTLSPlatform.util.JwtUtil;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // Register user
    public UserResponse register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullname(request.getFullname());
        user.setStatus("ACTIVE");
        user.setRole(Role.STUDENT);
        userRepository.save(user);
        return new UserResponse(user.getUsername(), user.getEmail(), user.getFullname(), user.getStatus());

    }


    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));

        return new AuthenticationResponse(
                user.getUsername(),
                user.getEmail(),
                user.getFullname(),
                user.getStatus(),
                jwt
        );
    }

    private Set<Role> extractRoles(UserDetails userDetails) {
        return Arrays.stream(userDetails.getAuthorities().toArray())
                .map(authority -> Role.valueOf(((GrantedAuthority) authority).getAuthority()))
                .collect(Collectors.toSet());
    }

}