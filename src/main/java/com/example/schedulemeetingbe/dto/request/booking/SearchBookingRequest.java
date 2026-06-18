package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record SearchBookingRequest(
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime start,

        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime end,

        BookingStatus status,

        String bookByName,

        String roomName

) {
}
