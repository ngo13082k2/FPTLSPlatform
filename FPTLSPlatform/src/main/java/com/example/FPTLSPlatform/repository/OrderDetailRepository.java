package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.OrderDetail;
import com.example.FPTLSPlatform.model.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    int countByClasses_ClassIdAndOrder_StatusNot(Long classId, OrderStatus status);

    @Query("SELECT od FROM OrderDetail od WHERE od.order.orderId = :orderId")
    Page<OrderDetail> findOrderDetailsByOrderId(@Param("orderId") Long orderId, Pageable pageable);

    Optional<OrderDetail> findByOrder_OrderId(Long orderId);

    OrderDetail findByOrder_User_UserNameAndClasses_ClassId(String username, Long classId);
    
    Page<OrderDetail> findByClasses_ClassId(Long classId, Pageable pageable);

    Page<OrderDetail> findByOrder_User_UserName(String username, Pageable pageable);

    Page<OrderDetail> findByOrderOrderId(Long orderId, Pageable pageable);

    List<OrderDetail> findByClasses_ClassId(Long classId);

}
