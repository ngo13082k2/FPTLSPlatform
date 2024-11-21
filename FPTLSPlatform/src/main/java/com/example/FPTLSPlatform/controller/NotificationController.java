package com.example.FPTLSPlatform.controller;

import com.example.FPTLSPlatform.dto.NotificationDTO;
import com.example.FPTLSPlatform.model.Notification;
import com.example.FPTLSPlatform.service.INotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final INotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationController(INotificationService notificationService, SimpMessagingTemplate messagingTemplate) {
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }

    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@RequestBody NotificationDTO notificationDto) {
        Notification notification = notificationService.createNotification(notificationDto);
        return ResponseEntity.ok(notification);
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        List<Notification> notifications = notificationService.getAllNotifications();
        messagingTemplate.convertAndSend("/topic/notifications", notifications);

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

    @GetMapping("/user/")
    public List<NotificationDTO> getNotifications() {
        String username = getCurrentUsername();
        List<Notification> notifications = notificationService.getNotificationByUsername(username);
        messagingTemplate.convertAndSend("/topic/notifications", notifications);

        return notifications.stream().map(NotificationDTO::fromEntity).collect(Collectors.toList());
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            notificationService.markAsRead(notificationId);

            messagingTemplate.convertAndSend("/topic/notifications", "Notification " + notificationId + " marked as read");
            return ResponseEntity.ok("Notification marked as read.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking notifications as read.");
        }
    }

    @PutMapping("/readAll")
    public ResponseEntity<String> markAllNotificationAsRead() {
        String username = getCurrentUsername();
        try {
            notificationService.markAllAsRead(username);
            messagingTemplate.convertAndSend("/topic/notifications", "All notifications for " + username + " marked as read");
            return ResponseEntity.ok("All notifications marked as read.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error marking notifications as read.");
        }
    }

}
