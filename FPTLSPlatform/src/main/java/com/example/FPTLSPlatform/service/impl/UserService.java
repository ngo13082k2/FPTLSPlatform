package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.model.Teacher;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.SystemWalletRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private TeacherRepository  teacherRepository;
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
    public List<User> getUsersByRoleStudent() {
        return userRepository.findByRole(Role.STUDENT).stream()
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
    public User createStaffUser(User user) {
        if (userRepository.existsByUserName(user.getUserName())) {
            throw new RuntimeException("Username already exists");
        }
        user.setRole(Role.STAFF);
        user.setCreatedDate(LocalDateTime.now());
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }
    public Teacher getTeacher(String teacherName) {
        return teacherRepository.findByTeacherName(teacherName)
                .orElseThrow(() -> new RuntimeException("Teacher not found with name: " + teacherName));
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
