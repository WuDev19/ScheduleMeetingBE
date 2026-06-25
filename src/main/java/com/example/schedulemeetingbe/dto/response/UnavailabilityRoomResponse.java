package com.example.schedulemeetingbe.dto.response;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record UnavailabilityRoomResponse(
        Long unId,
        String reason,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT_NO_TZ)
        OffsetDateTime start,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT_NO_TZ)
        OffsetDateTime end,
        RoomResponse room
) {
}
