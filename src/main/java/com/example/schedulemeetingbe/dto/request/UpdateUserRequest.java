package com.example.schedulemeetingbe.dto.request;

public record UpdateUserRequest(
        String username,
        String fullName,
        String phone
) {
}
