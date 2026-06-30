package com.example.schedulemeetingbe.entity.payload;

import java.util.List;

public record CancelBookingPayload (
        Long bookingId,
        String title,
        String address,
        String room,
        String startTime,
        String endTime,
        List<String> receivers,
        String reason
) {
}
