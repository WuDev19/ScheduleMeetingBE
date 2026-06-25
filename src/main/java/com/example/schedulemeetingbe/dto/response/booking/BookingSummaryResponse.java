package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;

public record BookingSummaryResponse(
        Long bookingId,
        Long historyId,
        String title,
        String userBooked,
        String phone,
        String roomName,
        BookingStatus status,
        BookingActionType actionType,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
