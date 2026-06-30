package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record UpdateBookingRequest(
        String title,
        String description,
        Integer attendeeCount,
        OffsetDateTime start,
        OffsetDateTime end,
        Boolean isCompleted,
        Long roomId,
        Long newRoomId
) {
}
