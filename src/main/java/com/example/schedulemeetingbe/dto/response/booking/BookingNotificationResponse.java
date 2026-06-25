package com.example.schedulemeetingbe.dto.response.booking;

import java.time.OffsetDateTime;

public record BookingNotificationResponse(
        Long id,
        String title,
        String description,
        Long roomId,
        String roomName,
        String roomAddress,
        Integer floorNumber,
        String userBooked,
        String phone,
        String email,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer attendee,
        OffsetDateTime createdAt,
        String titleNotification,
        String messageNotification
) {
}
