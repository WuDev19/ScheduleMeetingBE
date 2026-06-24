package com.example.schedulemeetingbe.dto.response.recurrence;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record CancelRecurrenceResponse(
        Long recurringId,
        String reason,
        BookingStatus status,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT_NO_TZ)
        ZonedDateTime cancelledAt
) {
}
