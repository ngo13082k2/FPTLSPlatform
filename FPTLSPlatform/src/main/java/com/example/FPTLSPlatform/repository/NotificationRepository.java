package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByOrderByNotificationIdDesc();


    List<Notification> findByUsernameOrderByNotificationIdDesc(String username);

    @Modifying
    @Query("UPDATE Notification n SET n.readStatus = true WHERE n.username = :username")
    void markAllAsReadByUsername(@Param("username") String username);

}
