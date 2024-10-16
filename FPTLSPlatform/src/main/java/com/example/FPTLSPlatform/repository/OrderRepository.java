package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    @Query("SELECT od FROM Order od WHERE od.user.userName = :username")
    Page<Order> findByUserName(@Param("username") String username, Pageable pageable);
}
