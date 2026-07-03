package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;

public interface RecurrencePatternStrategy {
    RecurrenceType getType();
    void create(RecurringPattern recurringPattern, RecurringPatternCreateRequest request, User register, Room room);
}
