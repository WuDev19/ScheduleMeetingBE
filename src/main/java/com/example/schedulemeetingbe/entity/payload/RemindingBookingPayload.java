package com.example.schedulemeetingbe.entity.payload;

import java.util.List;

public record RemindingBookingPayload(
        List<String> emails,
        String bookingTitle,
        String roomName,
        long minutes
) {
}
