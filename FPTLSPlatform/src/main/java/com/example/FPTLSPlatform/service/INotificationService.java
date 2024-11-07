package com.example.FPTLSPlatform.service;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;

import java.util.List;

public interface INotificationService {
    Notification createNotification(NotificationDTO notificationDto);

    List<Notification> getAllNotifications();

    Notification getNotificationById(Long id);

    void deleteNotification(Long id);

    List<Notification> getNotificationByUsername(String username);

    void markAsRead(Long notificationId);
}
