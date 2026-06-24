package com.example.schedulemeetingbe.dto.request.recurrence;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

public record ApproveRejectRecurringRequest(
        BookingStatus status,
        String reason
) {
}
