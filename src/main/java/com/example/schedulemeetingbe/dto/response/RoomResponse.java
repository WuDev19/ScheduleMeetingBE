package com.example.schedulemeetingbe.dto.response;

public record RoomResponse(
        Long id,
        String roomName,
        Integer capacity,
        Integer floorNumber,
        String description,
        BuildingResponse building
) {
}
