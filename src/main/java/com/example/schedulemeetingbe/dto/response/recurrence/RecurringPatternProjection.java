package com.example.schedulemeetingbe.dto.response.recurrence;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.LocalDate;

public interface RecurringPatternProjection {
     Long getRecurringId();
     Integer getInterval();
     String getDaysOfWeek();
     LocalDate getStartDate();
     LocalDate getEndDate();
     BookingStatus getStatus();
     Long getUserCreatedId();
     String getUserCreatedName();
}
