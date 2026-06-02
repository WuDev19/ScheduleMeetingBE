package com.example.schedulemeetingbe.dto.response;

public record DepartmentResponse(
        Long id,
        String name,
        String code,
        String description
) {
}
