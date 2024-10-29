package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.SystemWallet;
import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.model.enums.Role;
import com.example.FPTLSPlatform.repository.SystemWalletRepository;
import com.example.FPTLSPlatform.repository.TeacherRepository;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

}
