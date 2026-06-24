package com.example.schedulemeetingbe.dto.response.recurrence;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

public record ApproveRejectRecurrenceResponse(
        Long recurringId,
        BookingStatus status
) {
}
