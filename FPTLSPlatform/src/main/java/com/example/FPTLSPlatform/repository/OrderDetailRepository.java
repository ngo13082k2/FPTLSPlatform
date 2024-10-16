package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.OrderDetail;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    int countByClasses_ClassId(Long classId);

    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderId = :orderId")
    List<OrderDetail> findOrderDetailsByOrderId(@Param("orderId") Long orderId);

    Optional<OrderDetail> findByOrder_OrderId(Long orderId);

    boolean existsByOrder_User_UserNameAndClasses_ClassId(String username, Long classId);

    Page<OrderDetail> findByClasses_ClassId(Long classId, Pageable pageable);

    List<OrderDetail> findByOrder_User_UserName(String username);

    List<OrderDetail> findByOrderOrderId(Long orderId);

}
