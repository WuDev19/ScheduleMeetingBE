package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingRecurrenceResponse(
        Long bookingId,
        Long recurringId,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        BookingStatus status
) {
}
