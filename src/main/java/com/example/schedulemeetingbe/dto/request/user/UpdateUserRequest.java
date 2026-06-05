package com.example.schedulemeetingbe.dto.request.user;

public record UpdateUserRequest(
        String username,
        String fullName,
        String phone
) {
}
