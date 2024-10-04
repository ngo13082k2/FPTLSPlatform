package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.TransactionHistory;
import com.example.FPTLSPlatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUser(User user);
}