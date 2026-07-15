package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record UpdateBookingRequest(
        String title,
        String description,

        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime start,

        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime end,

        Boolean isCompleted,
        Long roomId,
        Long newRoomId
) {
}
