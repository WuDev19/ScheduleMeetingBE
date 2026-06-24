package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.*;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.User;

import java.time.ZonedDateTime;
import java.util.List;

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
                .recurringId(recurringPattern.getRecurringId())
                .status(recurringPattern.getStatus())
                .interval(recurringPattern.getIntervalValue())
                .startDate(recurringPattern.getStartDate())
                .endDate(recurringPattern.getEndDate())
                .userCreatedId(userId)
                .userCreatedName(userCreated)
                .daysOfWeek(recurringPattern.getDaysOfWeek())
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
                ZonedDateTime.now()
        );
    }

    public static ApproveRejectRecurrenceResponse mapToApproveOrRejectRecurrenceResponse(
            Long recurringId,
            BookingStatus status
    ) {
        return new ApproveRejectRecurrenceResponse(recurringId, status);
    }

    public static RecurringPatternResponse mapToRecurringPatternResponse(RecurringPatternProjection projection, List<BookingRecurrenceResponse> bookings) {
        return new RecurringPatternResponse(
                projection.getRecurringId(),
                projection.getInterval(),
                projection.getDaysOfWeek(),
                projection.getStartDate(),
                projection.getEndDate(),
                projection.getStatus(),
                projection.getUserCreatedId(),
                projection.getUserCreatedName(),
                bookings
        );
    }

    public static RecurringPatternResponse mapToRecurringPatternResponse(RecurringPattern recurringPattern, RecurrenceUserResponse userResponse, List<BookingRecurrenceResponse> bookings) {
        return new RecurringPatternResponse(
                recurringPattern.getRecurringId(),
                recurringPattern.getIntervalValue(),
                recurringPattern.getDaysOfWeek(),
                recurringPattern.getStartDate(),
                recurringPattern.getEndDate(),
                recurringPattern.getStatus(),
                userResponse.userId(),
                userResponse.fullName(),
                bookings
        );
    }

}
