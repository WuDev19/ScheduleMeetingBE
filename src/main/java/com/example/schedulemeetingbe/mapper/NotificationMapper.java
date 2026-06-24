package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.notification.NotificationResponse;
import com.example.schedulemeetingbe.entity.Notification;

public class NotificationMapper {
    private NotificationMapper(){}

    public static NotificationResponse mapToNotificationResponse(Notification notification, Long userId){
        return new NotificationResponse(
                notification.getNotificationId(),
                userId,
                notification.getTitle(),
                notification.getMessage(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
