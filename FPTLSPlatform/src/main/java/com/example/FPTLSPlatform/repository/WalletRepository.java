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

    @Query("SELECT MONTH(w.transactionDate) AS month, " +
            "MAX(w.balanceAfterTransaction) AS lastBalance " +
            "FROM SystemTransactionHistory w " +
            "WHERE YEAR(w.transactionDate) = :year " +
            "GROUP BY MONTH(w.transactionDate)")
    List<Object[]> getLastBalanceByMonth(@Param("year") Integer year);


    @Query("SELECT MONTH(w.transactionDate) AS month, " +
            "SUM(CASE WHEN w.transactionAmount > 0 THEN w.transactionAmount ELSE 0 END) AS totalDeposit, " +
            "SUM(CASE WHEN w.transactionAmount < 0 THEN ABS(w.transactionAmount) ELSE 0 END) AS totalWithdrawal " +
            "FROM SystemTransactionHistory w " +
            "WHERE YEAR(w.transactionDate) = :year " +
            "GROUP BY MONTH(w.transactionDate)")
    List<Object[]> getTransactionSummaryByMonth(@Param("year") Integer year);
}
