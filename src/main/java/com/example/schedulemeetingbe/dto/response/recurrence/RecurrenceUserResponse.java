package com.example.schedulemeetingbe.dto.response.recurrence;

public record RecurrenceUserResponse(
        Long recurringId,
        Long userId,
        String fullName
) {
}
