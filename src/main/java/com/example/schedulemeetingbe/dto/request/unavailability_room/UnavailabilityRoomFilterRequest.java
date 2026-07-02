package com.example.schedulemeetingbe.dto.request.unavailability_room;

import com.example.schedulemeetingbe.constant.StringCommon;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

public record UnavailabilityRoomFilterRequest(
        @DateTimeFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime start,

        @DateTimeFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime end
) {
}
