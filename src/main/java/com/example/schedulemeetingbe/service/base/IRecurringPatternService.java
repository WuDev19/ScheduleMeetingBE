package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;

public interface IRecurringPatternService {
    RecurringPatternResponse createRecurring(RecurringPatternCreateRequest request, Long userId);
    RecurringPattern save(RecurringPattern recurringPattern);
}
