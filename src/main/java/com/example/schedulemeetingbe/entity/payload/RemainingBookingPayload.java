package com.example.schedulemeetingbe.entity.payload;

import java.util.List;

public record RemainingBookingPayload(
        List<String> emails,
        String bookingTitle,
        String roomName,
        long minutes
) {
}
