package com.example.schedulemeetingbe.entity.payload;

public record UserResetPasswordPayload(
        Long userId,
        String email
) {
}
