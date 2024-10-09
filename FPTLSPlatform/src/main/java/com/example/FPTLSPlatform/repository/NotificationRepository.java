package com.example.FPTLSPlatform.repository;

import com.example.FPTLSPlatform.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification,Long> {
    List<Notification> findAllByOrderByNotificationIdDesc();
}
