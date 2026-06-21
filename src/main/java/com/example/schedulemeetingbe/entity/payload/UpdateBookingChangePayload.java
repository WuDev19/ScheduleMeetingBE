package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.ZonedDateTime;

public record UpdateBookingChangePayload(
        Long bookingId,
        String title,
        String description,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        Integer attendeeCount,
        BookingStatus status,
        String cancellationReason,
        Long roomId,
        Long bookedBy,
        ZonedDateTime createdAt
) {
}
