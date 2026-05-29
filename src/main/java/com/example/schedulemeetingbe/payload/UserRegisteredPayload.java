package com.example.schedulemeetingbe.payload;

public record UserRegisteredPayload(
        Long userId,
        String email,
        String token
) {
}
