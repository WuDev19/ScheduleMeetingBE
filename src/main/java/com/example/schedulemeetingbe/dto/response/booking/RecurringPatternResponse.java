package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.LocalDate;

public record RecurringPatternResponse(
        Long id,
        Integer interval,
        String daysOfWeek,
        LocalDate endDate,
        BookingStatus status,
        Long createdBy
) {
}
