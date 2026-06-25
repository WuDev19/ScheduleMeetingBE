package com.example.schedulemeetingbe.dto.request.unavailability_room;

import java.time.OffsetDateTime;

public record UpdateUnavailabilityRoomRequest(
        String reason,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
