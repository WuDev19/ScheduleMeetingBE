package com.example.schedulemeetingbe.dto.response;

public record EquipmentResponse(
        Long equipmentId,
        String equipmentName,
        String description,
        Integer availableQuantity
) {
}
