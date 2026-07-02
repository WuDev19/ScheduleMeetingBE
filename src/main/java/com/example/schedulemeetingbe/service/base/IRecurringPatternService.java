package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.ApproveRejectRecurringRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.CancelRecurringPatternRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternFilterRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.ApproveRejectRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.CancelRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.RecurringPatternResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IRecurringPatternService {
    RecurringPatternResponse createRecurring(RecurringPatternCreateRequest request, Long userId);

    CancelRecurrenceResponse cancelRecurring(Long recurringId, CancelRecurringPatternRequest request);

    ApproveRejectRecurrenceResponse approveOrRejectRecurring(Long recurringId, Long approverId, ApproveRejectRecurringRequest request);

    PageResponse<RecurringPatternResponse> getRecurringPatternWaiting(Pageable pageable);

    PageResponse<RecurringPatternResponse> getMyRecurringPattern(Long userId, Pageable pageable);

    PageResponse<RecurringPatternResponse> filter(Long userId, List<String> roles, RecurringPatternFilterRequest request, Pageable pageable);
}
