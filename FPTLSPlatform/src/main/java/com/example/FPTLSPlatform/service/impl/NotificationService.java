package com.example.FPTLSPlatform.service.impl;


import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.exception.ResourceNotFoundException;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.repository.NotificationRepository;
import com.example.FPTLSPlatform.service.INotificationService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
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
        NotificationDTO notificationDTO = NotificationDTO.fromEntity(notification);
        messagingTemplate.convertAndSendToUser(notificationDto.getUsername(), "/queue/notifications", notificationDTO);

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
        if (notification == null) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setReadStatus(true);
        notificationRepository.save(notification);
    }


    @Override
    public void markAllAsRead(String username) {
        notificationRepository.markAllAsReadByUsername(username);
    }

}
