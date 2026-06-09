package com.example.schedulemeetingbe.entity.payload;

import java.time.ZonedDateTime;

public record BookingCancelledByMaintenancePayload(
        Long bookingId,
        Long userId,
        Long roomId,
        String reason,
        ZonedDateTime startTime,
        ZonedDateTime endTime
) {
}
