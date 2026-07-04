package com.example.schedulemeetingbe.entity.payload;

import lombok.AccessLevel;
import lombok.Builder;

import java.util.List;

@Builder(access = AccessLevel.PUBLIC)
public record SimpleCancelBookingPayload(
        Long bookingId,
        String title,
        String startTime,
        String endTime,
        List<String> receivers,
        String reason
) {
}
