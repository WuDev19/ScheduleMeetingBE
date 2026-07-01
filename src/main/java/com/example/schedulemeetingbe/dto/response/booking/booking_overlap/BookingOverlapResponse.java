package com.example.schedulemeetingbe.dto.response.booking.booking_overlap;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingOverlapResponse(
        Long bookingId,
        String title,
        String userBooked,
        String phone,
        String roomName,
        BookingStatus status,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
