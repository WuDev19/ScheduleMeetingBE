package com.example.schedulemeetingbe.dto.request.booking;

import java.time.ZonedDateTime;

public record UpdateBookingRequest(
        String title,
        String description,
        Integer attendeeCount,
        ZonedDateTime start,
        ZonedDateTime end,
        Long roomId,
        Long newRoomId
) {
}
