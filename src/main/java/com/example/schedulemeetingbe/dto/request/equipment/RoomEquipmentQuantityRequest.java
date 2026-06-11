package com.example.schedulemeetingbe.dto.request.equipment;

public record RoomEquipmentQuantityRequest (
        Long equipmentId,
        Integer quantity
) {
}
