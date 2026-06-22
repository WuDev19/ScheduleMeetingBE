package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.User;

public class RecurringPatternMapper {
    private RecurringPatternMapper() {
    }

    public static RecurringPattern mapToRecurringPattern(RecurringPatternCreateRequest request, User user) {
        return RecurringPattern.builder()
                .recurrenceType(request.type())
                .daysOfWeek(request.dayOfWeeks())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .intervalValue(request.interval())
                .createdBy(user)
                .build();
    }

    public static RecurringPatternResponse mapToRecurringPatternResponse(
            RecurringPattern recurringPattern,
            Long userId,
            String userCreated
    ) {
        return RecurringPatternResponse.builder()
                .id(recurringPattern.getRecurringId())
                .status(recurringPattern.getStatus())
                .interval(recurringPattern.getIntervalValue())
                .startDate(recurringPattern.getStartDate())
                .endDate(recurringPattern.getEndDate())
                .userCreatedId(userId)
                .userCreatedName(userCreated)
                .daysOfWeek(recurringPattern.getDaysOfWeek())
                .build();
    }

}
