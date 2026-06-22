package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import lombok.AccessLevel;
import lombok.Builder;

import java.time.LocalDate;

@Builder(access = AccessLevel.PUBLIC)
public record RecurringPatternResponse(
        Long id,
        Integer interval,
        String daysOfWeek,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Long userCreatedId,
        String userCreatedName
) {
}
