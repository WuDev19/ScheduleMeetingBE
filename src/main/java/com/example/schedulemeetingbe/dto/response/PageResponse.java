package com.example.schedulemeetingbe.dto.response;

import java.util.List;

public record PageResponse<T>(
        int page,
        int sizeOfPage,
        long totalElements,
        int totalPages,
        List<T> content
) {
}
