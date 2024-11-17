package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.repository.NotificationRepository;
import com.example.FPTLSPlatform.service.INotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification createNotification(NotificationDTO notificationDto) {
        Notification notification = Notification.builder()
                .title(notificationDto.getTitle())
                .name(notificationDto.getName())
                .description(notificationDto.getDescription())
                .readStatus(false)
                .createAt(LocalDateTime.now())
                .username(notificationDto.getUsername())
                .type(notificationDto.getType())
                .build();
        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByNotificationIdDesc();
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }

    public void deleteNotification(Long id) {
        Notification notification = getNotificationById(id);
        notificationRepository.delete(notification);
    }

    @Override
    public List<Notification> getNotificationByUsername(String username) {
        return notificationRepository.findByUsernameOrderByNotificationIdDesc(username);
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = getNotificationById(notificationId);
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String username) {
        List<Notification> notifications = getNotificationByUsername(username);
        for (Notification notification : notifications) {
            notification.setReadStatus(true);
        }
        notificationRepository.saveAll(notifications);
    }
}
