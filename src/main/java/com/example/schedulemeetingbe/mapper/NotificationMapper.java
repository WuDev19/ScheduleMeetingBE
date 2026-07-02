package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.notification.NotificationResponse;
import com.example.schedulemeetingbe.entity.Notification;

public final class NotificationMapper {
    private NotificationMapper() {
    }

    public static NotificationResponse mapToNotificationResponse(Notification notification, Long userId, Long bookingId) {
        return new NotificationResponse(
                notification.getNotificationId(),
                userId,
                bookingId,
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
