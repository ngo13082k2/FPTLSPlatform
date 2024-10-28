package com.example.FPTLSPlatform.model.enums;

public enum OrderStatus {
    PENDING,
    CANCELLED,
    ACTIVED,
    COMPLETED;

    public static OrderStatus fromString(String status) {
        return OrderStatus.valueOf(status.toUpperCase());
    }
}