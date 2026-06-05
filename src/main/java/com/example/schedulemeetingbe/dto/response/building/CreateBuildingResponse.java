package com.example.schedulemeetingbe.dto.response.building;

public record CreateBuildingResponse(
        Long buildingId,
        String buildingName,
        String buildingAddress
) {
}
