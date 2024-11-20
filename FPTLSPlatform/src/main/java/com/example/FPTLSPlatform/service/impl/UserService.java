package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.Wallet;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.SystemWalletRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.repository.WalletRepository;
import com.example.FPTLSPlatform.request.RegisterRequest;
import com.example.FPTLSPlatform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SystemWalletRepository systemWalletRepository;
    @Autowired
    private TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private WalletRepository walletRepository;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void updateWalletBalance(String username, Double amount) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getWallet().setBalance(user.getWallet().getBalance() + amount);
        userRepository.save(user);
    }

    public Double getSystemWalletBalance() {
        SystemWallet systemWallet = systemWalletRepository.findById(1L)
                .orElse(new SystemWallet(1L, 0.0));
        return systemWallet.getTotalAmount();
    }

    public Map<String, Long> getUserCountByRole() {
        Map<String, Long> userCountByRole = new HashMap<>();

        userCountByRole.put("STAFF", userRepository.countByRole(Role.STAFF));
        userCountByRole.put("STUDENT", userRepository.countByRole(Role.STUDENT));
        userCountByRole.put("ADMIN", userRepository.countByRole(Role.ADMIN));

        long teacherCount = teacherRepository.count();
        userCountByRole.put("TEACHER", teacherCount);

        long totalUsers = userRepository.count() + teacherCount;
        userCountByRole.put("TOTAL_USERS", totalUsers);

        return userCountByRole;
    }

    public List<User> getUsersByRoleStudentAndStaff() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == Role.STUDENT || user.getRole() == Role.STAFF)
                .filter(user -> "ACTIVE".equals(user.getStatus()) || "DEACTIVATED".equals(user.getStatus()))
                .map(user -> {
                    User response = new User();
                    response.setUserName(user.getUserName());
                    response.setStatus(user.getStatus());
                    response.setRole(user.getRole());
                    return response;
                }).collect(Collectors.toList());
    }


    public User deactivateUser(String username) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        if ("ACTIVE".equals(user.getStatus())) {
            user.setStatus("DEACTIVATED");
        } else if ("DEACTIVATED".equals(user.getStatus())) {
            user.setStatus("ACTIVE");
        } else {
            throw new RuntimeException("User status is neither ACTIVE nor DEACTIVATED: " + user.getStatus());
        }

        return userRepository.save(user);
    }

    public User createStaffUser(RegisterRequest request) {
        if (userRepository.existsByUserName(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        Wallet wallet = Wallet.builder()
                .balance(0.0)
                .build();
        walletRepository.save(wallet);


        User user = User.builder()
                .userName(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Mã hóa mật khẩu
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .createdDate(LocalDateTime.now())
                .modifiedDate(LocalDateTime.now())
                .wallet(wallet)
                .role(Role.STAFF)
                .status("ACTIVE")
                .build();

        return userRepository.save(user); // Lưu User cùng Wallet
    }

    public Teacher getTeacher(String teacherName) {
        return teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found with name: " + teacherName));
    }

    public Map<String, List<Teacher>> getTeachersByStatus() {
        Map<String, List<Teacher>> result = new HashMap<>();
        result.put("ACTIVE", teacherRepository.findByStatus("ACTIVE"));
        result.put("DEACTIVATED", teacherRepository.findByStatus("DEACTIVATED"));
        return result;
    }


    public Teacher deactivateTeacher(String teacherName) {
        Teacher teacher = teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found with name: " + teacherName));

        if ("ACTIVE".equals(teacher.getStatus())) {
            teacher.setStatus("DEACTIVATED");
        } else if ("DEACTIVATED".equals(teacher.getStatus())) {
            teacher.setStatus("ACTIVE");
        } else {
            throw new RuntimeException("Teacher status is neither ACTIVE nor DEACTIVATED: " + teacher.getStatus());
        }

        teacher.setModifiedDate(LocalDateTime.now());
        return teacherRepository.save(teacher);
    }

}
