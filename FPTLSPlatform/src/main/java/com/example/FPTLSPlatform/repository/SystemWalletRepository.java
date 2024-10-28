package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.SystemWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemWalletRepository extends JpaRepository<SystemWallet, Long> {
}
