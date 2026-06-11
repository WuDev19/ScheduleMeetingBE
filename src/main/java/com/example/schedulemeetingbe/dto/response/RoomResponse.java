package com.example.schedulemeetingbe.dto.response;

import com.example.schedulemeetingbe.dto.response.equipment.RoomEquipmentResponse;

import java.util.List;

public record RoomResponse(
        Long id,
        String roomName,
        Integer capacity,
        Integer floorNumber,
        String description,
        BuildingResponse building,
        List<RoomEquipmentResponse> equipments
) {
}
