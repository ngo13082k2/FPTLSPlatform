package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final INotificationService notificationService;

    @Autowired
    public NotificationController(INotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationDTO notificationDto) {
        Notification notification = notificationService.createNotification(notificationDto);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Notification notification = notificationService.getNotificationById(id);
        return ResponseEntity.ok(notification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.ok("Notification deleted successfully");
    }

    @GetMapping("/user/{username}")
    public List<NotificationDTO> getNotifications(@PathVariable String username) {
        List<Notification> notifications = notificationService.getNotificationByUsername(username);
        return notifications.stream().map(NotificationDTO::fromEntity).collect(Collectors.toList());
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok("Notification marked as read.");
    }
}
