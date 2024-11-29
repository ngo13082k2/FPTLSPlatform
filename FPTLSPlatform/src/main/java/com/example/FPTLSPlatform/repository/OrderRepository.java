package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.dto.TotalOrderDTO;
import com.example.FPTLSPlatform.model.Order;
import com.example.FPTLSPlatform.model.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT od FROM Order od WHERE od.user.userName = :username")
    Page<Order> findByUserName(@Param("username") String username, Pageable pageable);

    @Query("SELECT new com.example.FPTLSPlatform.dto.TotalOrderDTO(SUM(o.totalPrice), COUNT(o)) " +
            "FROM Order o WHERE o.createAt BETWEEN :startDate AND :endDate")
    TotalOrderDTO getTotalOrdersAndAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT od FROM OrderDetail od WHERE od.order.createAt BETWEEN :startDate AND :endDate")
    List<OrderDetail> getOrderDetailsByDateRange(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate);

    @Query("SELECT MONTH(o.createAt) AS month, SUM(o.totalPrice) AS totalOrders " +
            "FROM Order o " +
            "WHERE YEAR(o.createAt) = :year AND o.status = 'COMPLETED' " +
            "GROUP BY MONTH(o.createAt) " +
            "ORDER BY MONTH(o.createAt)")
    List<Object[]> getTotalOrderByMonth(@Param("year") Integer year);
}
