package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.OffsetDateTime;

// đang để record để tối ưu bộ nhớ tuy nhiên sẽ ko tái sử dụng field đc, nào có time sẽ xem xét lại kế thừa 1 lớp base
// có thể đây sẽ là lớp base nếu sau refactor
public record UpdateBookingChangePayload(
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
        OffsetDateTime createdAt
) {
}
