package com.example.schedulemeetingbe.dto.response;

public record UserDetailResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        DepartmentResponse department
) {
}
