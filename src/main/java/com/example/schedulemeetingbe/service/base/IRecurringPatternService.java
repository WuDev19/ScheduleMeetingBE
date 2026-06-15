package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;

public interface IRecurringPatternService {
    RecurringPatternResponse createForAPI(RecurringPatternCreateRequest request);
    RecurringPattern save(RecurringPattern recurringPattern);
}
