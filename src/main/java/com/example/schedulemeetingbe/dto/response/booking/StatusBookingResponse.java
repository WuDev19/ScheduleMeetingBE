package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;

public record StatusBookingResponse (
        Long bookingId,
        BookingStatus status,
        OffsetDateTime updatedAt
) {
}
