package com.example.schedulemeetingbe.dto.request.unavailability_room;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record UpdateUnavailabilityRoomRequest(
        String reason,
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime startTime,
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime endTime
) {
}
