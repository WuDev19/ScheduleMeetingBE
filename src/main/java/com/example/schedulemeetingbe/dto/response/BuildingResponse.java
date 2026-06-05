package com.example.schedulemeetingbe.dto.response;

public record BuildingResponse(
        Long id,
        String buildingName,
        String buildingAddress
) {
}
