package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.SystemTransactionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemTransactionHistoryRepository extends JpaRepository<SystemTransactionHistory, Long> {
}
