package com.example.schedulemeetingbe.dto.request.room;

public record UpdateRoomRequest(
        String roomName,
        Integer capacity,
        Integer floorNumber,
        String description
) {
}
