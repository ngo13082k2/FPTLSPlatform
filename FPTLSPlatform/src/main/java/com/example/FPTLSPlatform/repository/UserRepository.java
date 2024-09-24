package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {



    Optional<User> findByUserName(String username);


    Optional<User> findByEmail(String email);

    boolean existsByUserName(String username);
    Optional<User> findByPhoneNumber(String phoneNumber);
    boolean existsByEmail(String email);
}