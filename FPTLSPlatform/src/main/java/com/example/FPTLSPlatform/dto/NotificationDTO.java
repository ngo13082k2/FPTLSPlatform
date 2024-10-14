package com.example.FPTLSPlatform.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationDTO {
    private String name;
    private String title;
    private String description;
}
