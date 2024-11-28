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

    @Query(value = "SELECT " +
            "  MONTH(w.transaction_date) AS month, " +
            "  (SELECT w2.balance_after_transaction " +
            "   FROM system_transaction_history w2 " +
            "   WHERE YEAR(w2.transaction_date) = :year " +
            "     AND MONTH(w2.transaction_date) = MONTH(w.transaction_date) " +
            "   ORDER BY w2.transaction_date DESC " +
            "   LIMIT 1) AS balanceAfterLastTransaction, " +
            "  SUM(CASE WHEN w.transaction_amount > 0 THEN w.transaction_amount ELSE 0 END) AS totalDeposit, " +
            "  SUM(CASE WHEN w.transaction_amount < 0 THEN ABS(w.transaction_amount) ELSE 0 END) AS totalWithdrawal " +
            "FROM system_transaction_history w " +
            "WHERE YEAR(w.transaction_date) = :year " +
            "GROUP BY MONTH(w.transaction_date) " +
            "ORDER BY MONTH(w.transaction_date)", nativeQuery = true)
    List<WalletStatisticDTO> getWalletStatisticByMonth(@Param("year") Integer year);


}
