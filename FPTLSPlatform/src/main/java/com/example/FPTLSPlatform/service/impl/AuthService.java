package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.model.Category;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.CategoryRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.repository.WalletRepository;
import com.example.FPTLSPlatform.request.AuthenticationRequest;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.response.AuthenticationResponse;
import com.example.FPTLSPlatform.response.UserResponse;
import com.example.FPTLSPlatform.service.IEmailService;
import com.example.FPTLSPlatform.util.JwtUtil;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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
    private final OTPGmailService otpGmailService;
    private final IEmailService emailService;
    private final CategoryRepository categoryRepository;
    @Autowired
    private HttpSession session;
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil,
                       CustomUserDetailsService userDetailsService, TeacherRepository teacherRepository, WalletRepository walletRepository, OTPGmailService otpGmailService, IEmailService emailService, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;

        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.teacherRepository = teacherRepository;
        this.walletRepository = walletRepository;
        this.otpGmailService = otpGmailService;
        this.emailService = emailService;
        this.categoryRepository = categoryRepository;
    }

    public UserResponse register(RegisterRequest request) throws MessagingException {
        Optional<User> existingUserByEmail = userRepository.findByEmail(request.getEmail());
        Optional<User> existingUserByPhone = userRepository.findByPhoneNumber(request.getPhoneNumber());
        Optional<User> existingUserByUsername = userRepository.findByUserName(request.getUsername());
        if (existingUserByUsername.isPresent()) {
            throw new IllegalArgumentException("Username đã tồn tại.");
        }

        if ((existingUserByEmail.isPresent() && "ACTIVE".equals(existingUserByEmail.get().getStatus())) ||
                (existingUserByPhone.isPresent() && "ACTIVE".equals(existingUserByPhone.get().getStatus()))) {
            throw new IllegalArgumentException("Email hoặc số điện thoại đã tồn tại với trạng thái ACTIVE.");
        }

        User user = existingUserByEmail.orElseGet(() -> existingUserByPhone.orElse(null));
        if (user != null && "PENDING".equals(user.getStatus())) {
            user.setUserName(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setFullName(request.getFullName());
            user.setPhoneNumber(request.getPhoneNumber());
            user.setCreatedDate(LocalDateTime.now());
        } else {
            Wallet wallet = new Wallet();
            wallet.setBalance(0.0);
            walletRepository.save(wallet);

            user = User.builder()
                    .userName(request.getUsername())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .fullName(request.getFullName())
                    .phoneNumber(request.getPhoneNumber())
                    .createdDate(LocalDateTime.now())
                    .status("PENDING")
                    .wallet(wallet)
                    .role(Role.STUDENT)
                    .build();
        }
        List<Category> selectedCategoriesList = categoryRepository.findAllById(request.getCategoryIds());
        Set<Category> selectedCategories = new HashSet<>(selectedCategoriesList);
        user.setMajor(selectedCategories);
        userRepository.save(user);
        Set<String> categoryNames = selectedCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        int otp = otpGmailService.generateOTP(request.getEmail());
        emailService.sendOTP(request.getEmail(), otp);

        session.setAttribute("email", request.getEmail());

        return new UserResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), user.getPhoneNumber(), user.getRole(), categoryNames);
    }


    public UserResponse confirmOTP(int otp) {
        String email = (String) session.getAttribute("email");

        if (email == null) {
            throw new IllegalArgumentException("Không có email trong session");
        }

        Integer storedOtp = otpGmailService.getOTP(email);

        if (storedOtp == null || storedOtp != otp) {
            throw new IllegalArgumentException("OTP không hợp lệ hoặc đã hết hạn");
        }

        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
        user.setStatus("ACTIVE");
        userRepository.save(user);

        session.removeAttribute("email");
        session.invalidate();

        otpGmailService.clearOTP(email);

        return new UserResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), user.getPhoneNumber(), user.getRole());
    }

    public UserResponse registerTeacher(RegisterRequest request, HttpSession session) {
        if (teacherRepository.existsByTeacherName(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (teacherRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already exists");
        }
        if (teacherRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Wallet wallet = new Wallet();
        wallet.setBalance(0.0);
        walletRepository.save(wallet);

        Teacher teacher = Teacher.builder()
                .teacherName(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .fullName(request.getFullName())
                .createdDate(LocalDateTime.now())
                .role(Role.TEACHER)
                .wallet(wallet)
                .status("PENDING")
                .build();
        List<Category> selectedCategoriesList = categoryRepository.findAllById(request.getCategoryIds());
        Set<Category> selectedCategories = new HashSet<>(selectedCategoriesList);
        teacher.setMajor(selectedCategories);
        teacherRepository.save(teacher);
        Set<String> categoryNames = selectedCategories.stream()
                .map(Category::getName)
                .collect(Collectors.toSet());
        session.setAttribute("teacher", teacher);
        return new UserResponse(teacher.getTeacherName(), teacher.getEmail(), teacher.getFullName(), teacher.getStatus(), teacher.getPhoneNumber(), teacher.getRole(), categoryNames);
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

            if (user.getRole() == Role.STUDENT && "PENDING".equals(user.getStatus())) {
                throw new RuntimeException("Your account has not been approved as a student");
            }

            String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));
            return new AuthenticationResponse(user.getUserName(), user.getEmail(), user.getFullName(), user.getStatus(), jwt, user.getRole());

        } else if (optionalTeacher.isPresent()) {
            Teacher teacher = optionalTeacher.get();

            if ("PENDING".equals(teacher.getStatus())) {
                throw new RuntimeException("Your account has not been approved as a teacher");
            }

            String jwt = jwtUtil.generateToken(userDetails.getUsername(), extractRoles(userDetails));
            return new AuthenticationResponse(teacher.getTeacherName(), null, teacher.getFullName(), teacher.getStatus(), jwt, teacher.getRole());

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