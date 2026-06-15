package com.example.schedulemeetingbe.dto.response.equipment;

public record EquipmentAndQuantityResponse(
        Long equipmentId,
        String equipmentName,
        Integer remainingQuantity
) {
}
