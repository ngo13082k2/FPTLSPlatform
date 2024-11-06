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
    @Query("SELECT AVG(w.balance), SUM(w.balance) FROM Wallet w")
    Object[] getTotalAndAverageBalance();

    @Query("SELECT new com.example.FPTLSPlatform.dto.WalletStatisticDTO(MONTH(w.transactionDate), " +
            "AVG(w.transactionBalance), SUM(w.transactionBalance)," +
            "SUM(CASE WHEN w.note = 'Refunded' THEN w.amount ELSE 0 END), " +
            "AVG(CASE WHEN w.note = 'Deposit' THEN w.amount ELSE 0 END), " +
            "SUM(CASE WHEN w.note = 'Deposit' THEN w.amount ELSE 0 END), " +
            "SUM(CASE WHEN w.note = 'Order' THEN w.amount ELSE 0 END))" +
            "FROM TransactionHistory w " +
            "WHERE YEAR(w.transactionDate) = :year " +
            "GROUP BY MONTH(w.transactionDate) " +
            "ORDER BY MONTH(w.transactionDate)")
    List<WalletStatisticDTO> getWalletStatisticByMonth(@Param("year") Integer year);
}
