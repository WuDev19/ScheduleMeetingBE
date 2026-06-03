package com.example.schedulemeetingbe.entity.payload;

public record UserCreatePayload(
        Long userId,
        String email,
        String username,
        String password
) {
}
