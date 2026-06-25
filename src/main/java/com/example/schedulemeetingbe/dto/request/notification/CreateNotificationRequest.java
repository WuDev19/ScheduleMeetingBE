package com.example.schedulemeetingbe.dto.request.notification;

import java.util.List;

public record CreateNotificationRequest(
        List<Long> userIds,
        Long departmentId,
        String title,
        String message
) {
}
