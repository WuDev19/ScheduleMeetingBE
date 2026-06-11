package com.example.schedulemeetingbe.dto.response.equipment;

public record EquipmentResponse(
        Long equipmentId,
        String equipmentName,
        String description,
        Integer availableQuantity
) {
}
