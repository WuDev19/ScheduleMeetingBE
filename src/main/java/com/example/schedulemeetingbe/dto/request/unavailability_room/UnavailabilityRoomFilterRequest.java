package com.example.schedulemeetingbe.dto.request.unavailability_room;

import java.time.OffsetDateTime;

public record UnavailabilityRoomFilterRequest(
        OffsetDateTime start,
        OffsetDateTime end
) {
}
