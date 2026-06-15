package com.example.schedulemeetingbe.dto.response.booking;

import java.time.ZonedDateTime;

public record BookingResponse(
    Long id,
    String title,
    String description,
    String roomName,
    String roomAddress,
    Integer floorNumber,
    String userBooked,
    String phone,
    String email,
    ZonedDateTime startTime,
    ZonedDateTime endTime,
    Integer attendee,
    ZonedDateTime createdAt
) {
}
