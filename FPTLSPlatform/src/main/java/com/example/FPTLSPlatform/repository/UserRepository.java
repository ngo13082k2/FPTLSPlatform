package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {



    Optional<User> findByUsername(String username);


    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    Optional<User> findByPhonenumber(String phonenumber);
    boolean existsByEmail(String email);
}