package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public Notification createNotification(NotificationDTO notificationDto) {
        Notification notification = Notification.builder()
                .name(notificationDto.getName())
                .title(notificationDto.getTitle())
                .description(notificationDto.getDescription())
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
}
