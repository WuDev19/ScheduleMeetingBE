package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import java.time.OffsetDateTime;

public record BookingResponse(
    Long id,
    String title,
    String description,
    String roomName,
    String roomAddress,
    Integer floorNumber,
    String userBooked,
    String username,
    String phone,
    String email,
    OffsetDateTime startTime,
    OffsetDateTime endTime,
    Integer attendee,
    OffsetDateTime createdAt,
    BookingStatus status
) {
}
