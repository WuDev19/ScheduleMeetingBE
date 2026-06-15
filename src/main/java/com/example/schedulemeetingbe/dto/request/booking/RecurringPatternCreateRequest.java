package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;

import java.time.LocalDate;

public record RecurringPatternCreateRequest(
        RecurrenceType type,
        Integer interval,
        String dayOfWeeks,
        LocalDate endDate,
        Integer userId
) {
}
