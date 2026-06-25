package com.example.schedulemeetingbe.entity.payload;

public record ApproveRejectRecurrencePayload(
        String email,
        String title,
        String message
) {
}
