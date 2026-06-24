package com.example.schedulemeetingbe.dto.response.booking;

import java.time.ZonedDateTime;

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
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        Integer attendee,
        ZonedDateTime createdAt,
        String titleNotification,
        String messageNotification
) {
}
