package com.example.schedulemeetingbe.entity.payload;

import java.time.OffsetDateTime;

public record BookingCancelledByMaintenancePayload(
        Long bookingId,
        Long userId,
        Long roomId,
        String reason,
        OffsetDateTime startTime,
        OffsetDateTime endTime
) {
}
