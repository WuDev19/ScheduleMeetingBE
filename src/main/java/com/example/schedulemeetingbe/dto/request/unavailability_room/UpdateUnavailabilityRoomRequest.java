package com.example.schedulemeetingbe.dto.request.unavailability_room;

public record UpdateUnavailabilityRoomRequest(
        String reason,
        Long startTime,
        Long endTime
) {
}
