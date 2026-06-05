package com.example.schedulemeetingbe.dto.request.building;

public record UpdateBuildingRequest(
        String buildingName,
        String buildingAddress
) {
}
