package com.example.schedulemeetingbe.entity.payload;

public record UserChangeEmailPayload(
        Long userId,
        String newEmail,
        String token
) {
}
