package com.example.FPTLSPlatform.dto;

import com.example.FPTLSPlatform.model.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class NotificationDTO {
    private String name;

    private String title;

    private String description;

    private String type;

    private String username;

    private LocalDateTime createAt;

    private boolean readStatus;

    public static NotificationDTO fromEntity(Notification notification) {
        return NotificationDTO.builder()
                .name(notification.getName())
                .title(notification.getTitle())
                .description(notification.getDescription())
                .type(notification.getType())
                .username(notification.getUsername())
                .createAt(notification.getCreateAt())
                .readStatus(notification.isReadStatus())
                .build();
    }
}
