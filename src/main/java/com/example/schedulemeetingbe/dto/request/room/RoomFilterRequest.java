package com.example.schedulemeetingbe.dto.request.room;

public record RoomFilterRequest(
        Integer capacity,
        Integer floorNumber
) {
}
