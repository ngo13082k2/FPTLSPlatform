package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.ApprovalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRecordRepository extends JpaRepository<ApprovalRecord, Long> {
    Optional<ApprovalRecord> findByApplicationUser_ApplicationUserId(Long applicationUserId);

}
