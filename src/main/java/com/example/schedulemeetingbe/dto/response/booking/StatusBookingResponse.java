package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.ZonedDateTime;

public record StatusBookingResponse (
        Long bookingId,
        BookingStatus status,
        ZonedDateTime updatedAt
) {
}
