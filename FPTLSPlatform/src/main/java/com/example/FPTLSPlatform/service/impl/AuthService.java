package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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

    public UserResponse register(RegisterRequest request, Role role) {
        User user = new User();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        if (role == Role.TEACHER) {
            user.setStatus("PENDING");
            user.setRole(Role.TEACHER);
        } else if (role == Role.STUDENT) {
            user.setStatus("ACTIVE");
            user.setRole(Role.STUDENT);
        }
        userRepository.save(user);
        return new UserResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), user.getPhoneNumber());

    }



    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUserName(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() == Role.TEACHER && "PENDING".equals(user.getStatus())) {
            throw new RuntimeException("Your account has not been approved as a teacher");
        }

        String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));

        return new AuthenticationResponse(
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                jwt
        );
    }

    public UserResponse viewCurrentUser(String token) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getAddress(),
                user.getCreatedDate(),
                user.getModifiedDate(),
                user.getPhoneNumber()

        );
    }
    public UserResponse updateCurrentUser(String token, AuthenticationRequest request) {
        String username = jwtUtil.extractUsername(token.substring(7));
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFullName(request.getFullName());
        user.setAddress(request.getAddress());
        user.setModifiedDate(LocalDateTime.now());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        userRepository.save(user);

        return new UserResponse(
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getAddress(),
                user.getCreatedDate(),
                user.getModifiedDate(),
                user.getPhoneNumber()

        );
    }
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        return new UserResponse(
                user.getUserName(),
                user.getEmail(),
                user.getFullName(),
                user.getStatus(),
                user.getAddress(),
                user.getCreatedDate(),
                user.getModifiedDate(),
                user.getPhoneNumber()
     );
    }




    private Set<Role> extractRoles(UserDetails userDetails) {
        return Arrays.stream(userDetails.getAuthorities().toArray())
                .map(authority -> Role.valueOf(((GrantedAuthority) authority).getAuthority()))
                .collect(Collectors.toSet());
    }

}