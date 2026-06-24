package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.ZonedDateTime;

public record BookingRecurrenceResponse(
        Long bookingId,
        Long recurringId,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        BookingStatus status
) {
}
