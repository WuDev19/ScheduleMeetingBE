package com.example.schedulemeetingbe.dto.response;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.ZonedDateTime;

public record UnavailabilityRoomResponse(
        Long unId,
        String reason,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT)
        ZonedDateTime start,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT)
        ZonedDateTime end,
        RoomResponse room
) {
}
