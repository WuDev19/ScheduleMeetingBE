package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

public record BookingFilterRequest(
        Long roomId,
        Long bookedBy,
        BookingStatus status,
        String fromDate,
        String toDate
) {
}
