package com.example.schedulemeetingbe.dto.response.notification;

import java.time.OffsetDateTime;

public record NotificationResponse(
        Long notificationId,
        Long userId,
        Long bookingId,
        String title,
        String message,
        Boolean isRead,
        OffsetDateTime createdAt
) {
}
