package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.*;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.utils.TimeUtils;

import java.util.List;

public final class RecurringPatternMapper {
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
                .recurringId(recurringPattern.getRecurringId())
                .status(recurringPattern.getStatus())
                .interval(recurringPattern.getIntervalValue())
                .startDate(recurringPattern.getStartDate())
                .endDate(recurringPattern.getEndDate())
                .userCreatedId(userId)
                .userCreatedName(userCreated)
                .daysOfWeek(recurringPattern.getDaysOfWeek())
                .recurrenceType(recurringPattern.getRecurrenceType())
                .build();
    }

    public static CancelRecurrenceResponse mapToCancelRecurrenceResponse(
            Long recurringId,
            String reason
    ) {
        return new CancelRecurrenceResponse(
                recurringId,
                reason,
                BookingStatus.CANCELLED,
                TimeUtils.now()
        );
    }

    public static ApproveRejectRecurrenceResponse mapToApproveOrRejectRecurrenceResponse(
            Long recurringId,
            BookingStatus status
    ) {
        return new ApproveRejectRecurrenceResponse(recurringId, status);
    }

    public static RecurringPatternResponse mapToRecurringPatternResponse(RecurringPatternProjection projection, List<BookingRecurrenceResponse> bookings) {
        return RecurringPatternResponse.builder()
                .recurringId(projection.getRecurringId())
                .interval(projection.getInterval())
                .daysOfWeek(projection.getDaysOfWeek())
                .startDate(projection.getStartDate())
                .endDate(projection.getEndDate())
                .status(projection.getStatus())
                .userCreatedId(projection.getUserCreatedId())
                .userCreatedName(projection.getUserCreatedName())
                .bookings(bookings)
                .recurrenceType(projection.getRecurrenceType())
                .build();
    }

    public static RecurringPatternResponse mapToRecurringPatternResponse(RecurringPattern recurringPattern, RecurrenceUserResponse userResponse, List<BookingRecurrenceResponse> bookings) {
        return RecurringPatternResponse.builder()
                .recurringId(recurringPattern.getRecurringId())
                .interval(recurringPattern.getIntervalValue())
                .daysOfWeek(recurringPattern.getDaysOfWeek())
                .startDate(recurringPattern.getStartDate())
                .endDate(recurringPattern.getEndDate())
                .status(recurringPattern.getStatus())
                .userCreatedId(userResponse.userId())
                .userCreatedName(userResponse.fullName())
                .bookings(bookings)
                .recurrenceType(recurringPattern.getRecurrenceType())
                .build();
    }

}
