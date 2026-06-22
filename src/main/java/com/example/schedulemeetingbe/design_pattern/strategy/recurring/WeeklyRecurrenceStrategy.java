package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyRecurrenceStrategy implements RecurrencePatternStrategy {
    @Override
    public RecurrenceType getType() {
        return RecurrenceType.WEEKLY;
    }

    @Override
    public void create(RecurringPattern recurringPattern, RecurringPatternCreateRequest request, User register, Room room) {
    }
}
