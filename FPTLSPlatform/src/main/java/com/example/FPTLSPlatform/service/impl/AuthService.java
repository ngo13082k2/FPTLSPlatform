package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.repository.WalletRepository;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.util.JwtUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service

public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final TeacherRepository teacherRepository;
    private final WalletRepository walletRepository;
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       CustomUserDetailsService userDetailsService, TeacherRepository teacherRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.teacherRepository = teacherRepository;
        this.walletRepository = walletRepository;
    }

    public UserResponse register(RegisterRequest request, Role role) {
        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);

        walletRepository.save(wallet);

        User user = new User();
        user.setUserName(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setCreatedDate(request.getCreatedDate());
        user.setCreatedDate(LocalDateTime.now());

        user.setWallet(wallet);

        if (role == Role.STUDENT) {
            user.setStatus("ACTIVE");
            user.setRole(Role.STUDENT);
        }

        userRepository.save(user);

        return new UserResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), user.getPhoneNumber(), user.getRole());
    }

    public UserResponse registerTeacher(RegisterRequest request, HttpSession session) {
        if (teacherRepository.existsByTeacherName(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (teacherRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }

        Teacher teacher = Teacher.builder()
                .teacherName(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .createdDate(LocalDateTime.now())
                .role(Role.TEACHER)
                .status("PENDING")
                .build();

        teacherRepository.save(teacher);
        session.setAttribute("teacher", teacher);
        return new UserResponse(teacher.getTeacherName(), teacher.getEmail(), teacher.getFullName(), teacher.getStatus(), teacher.getPhoneNumber(), teacher.getRole());
    }




    public AuthenticationResponse login(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        Optional<User> optionalUser = userRepository.findByUserName(request.getUsername());
        Optional<Teacher> optionalTeacher = teacherRepository.findByTeacherName(request.getUsername());

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getRole() == Role.TEACHER && "PENDING".equals(user.getStatus())) {
                throw new RuntimeException("Your account has not been approved as a teacher");
            }
            String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));
            return new AuthenticationResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), jwt,user.getRole());
        } else if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();
            if ("PENDING".equals(teacher.getStatus())) {
                throw new RuntimeException("Your account has not been approved as a teacher");
            }
            String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));
            return new AuthenticationResponse(teacher.getTeacherName(), null, teacher.getFullName(), teacher.getStatus(), jwt,teacher.getRole());
        } else {
            throw new RuntimeException("User not found");
        }
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
    public UserResponse getUserByUserName(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + username));

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