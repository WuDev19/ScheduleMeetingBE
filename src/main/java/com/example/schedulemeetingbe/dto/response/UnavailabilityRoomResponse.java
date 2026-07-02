package com.example.schedulemeetingbe.dto.response;

import java.time.OffsetDateTime;

public record UnavailabilityRoomResponse(
        Long unId,
        String reason,
        OffsetDateTime start,
        OffsetDateTime end,
        RoomResponse room
) {
}
