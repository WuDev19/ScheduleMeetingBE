package com.example.schedulemeetingbe.dto.request.building;

public record BuildingResponse(
        Long id,
        String buildingName,
        String buildingAddress
) {
}
