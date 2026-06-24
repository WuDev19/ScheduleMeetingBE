package com.example.schedulemeetingbe.dto.response.notification;

import java.time.ZonedDateTime;

public record NotificationResponse(
        Long notificationId,
        Long userId,
        String title,
        String message,
        Boolean isRead,
        ZonedDateTime createdAt
) {
}
