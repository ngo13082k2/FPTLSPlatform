package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.service.INotificationService;
import com.example.FPTLSPlatform.service.impl.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private INotificationService notificationService;
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
}
