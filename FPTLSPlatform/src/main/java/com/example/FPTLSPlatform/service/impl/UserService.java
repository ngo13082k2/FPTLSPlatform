package com.example.FPTLSPlatform.service.impl;

import com.example.FPTLSPlatform.model.User;
import com.example.FPTLSPlatform.repository.UserRepository;
import com.example.FPTLSPlatform.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class UserService implements IUserService {

    @Autowired
    private UserRepository userRepository;

    public void updateWalletBalance(String username, Double amount) {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.getWallet().setBalance(user.getWallet().getBalance() + amount);
        userRepository.save(user);
    }
}
