package com.example.schedulemeetingbe.dto.request.booking;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record UpdateBookingRequest(
        String title,
        String description,
        Integer attendeeCount,
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime start,
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime end,

        Long roomId,

        Long newRoomId
) {
}
