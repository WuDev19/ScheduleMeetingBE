package com.example.schedulemeetingbe.dto.request.equipment;

public record UpdateEquipmentRequest(
        String equipmentName,
        String description,
        Integer availableQuantity
) {
}
