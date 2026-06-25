package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateBookingPayload (
        Long bookingId,
        String title,
        String description,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer attendeeCount,
        BookingStatus status,
        String cancellationReason,
        Long roomId,
        Long bookedBy,
        OffsetDateTime createdAt,
        List<String> emails
) {
}
