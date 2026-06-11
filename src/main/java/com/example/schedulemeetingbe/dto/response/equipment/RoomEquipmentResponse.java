package com.example.schedulemeetingbe.dto.response.equipment;

public record RoomEquipmentResponse (
        Long roomId,
        Long equipmentId,
        String equipmentName,
        String description,
        Integer quantity
){
}
