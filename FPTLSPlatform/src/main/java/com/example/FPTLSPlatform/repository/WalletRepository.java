package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.dto.WalletStatisticDTO;
import com.example.FPTLSPlatform.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    @Query("SELECT new com.example.FPTLSPlatform.dto.WalletStatisticDTO( " +
            "MONTH(w.transactionDate), " +
            "(SELECT w2.balanceAfterTransaction FROM SystemTransactionHistory w2 " +
            " WHERE YEAR(w2.transactionDate) = :year AND MONTH(w2.transactionDate) = MONTH(w.transactionDate) " +
            " ORDER BY w2.transactionDate DESC LIMIT 1), " + // Số dư cuối cùng trong tháng
            "SUM(CASE WHEN w.transactionAmount > 0 THEN w.transactionAmount ELSE 0 END), " + // Tổng thu nhập
            "SUM(CASE WHEN w.transactionAmount < 0 THEN ABS(w.transactionAmount) ELSE 0 END)) " + // Tổng chi phí
            "FROM SystemTransactionHistory w " +
            "WHERE YEAR(w.transactionDate) = :year " +
            "GROUP BY MONTH(w.transactionDate) " +
            "ORDER BY MONTH(w.transactionDate)")
    List<WalletStatisticDTO> getWalletStatisticByMonth(@Param("year") Integer year);

}
