package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record SearchBookingRequest(
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime start,

        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime end,

        BookingStatus status,

        String bookByName,

        String roomName

) {
}
