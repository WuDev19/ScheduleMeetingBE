package com.example.schedulemeetingbe.dto.request.recurrence;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.RecurrenceType;

import java.time.LocalDate;

public record RecurringPatternFilterRequest(
        BookingStatus status,
        LocalDate startDate,
        LocalDate endDate,
        RecurrenceType recurrenceType,
        Long userCreatedId
) {
}
