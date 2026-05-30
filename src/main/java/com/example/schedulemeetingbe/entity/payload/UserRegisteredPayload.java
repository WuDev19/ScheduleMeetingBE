package com.example.schedulemeetingbe.entity.payload;

public record UserRegisteredPayload(
        Long userId,
        String email,
        String token
) {
}
