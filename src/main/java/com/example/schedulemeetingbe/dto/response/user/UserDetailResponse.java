package com.example.schedulemeetingbe.dto.response.user;

import com.example.schedulemeetingbe.dto.response.DepartmentResponse;

public record UserDetailResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String avatarUrl,
        Boolean isActive,
        DepartmentResponse department
) {
}
