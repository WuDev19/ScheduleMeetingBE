package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.EquipmentResponse;
import com.example.schedulemeetingbe.entity.Equipment;

public class EquipmentMapper {
    private EquipmentMapper() {
    }

    public static EquipmentResponse mapToEquipmentResponse(Equipment equipment) {
        return new EquipmentResponse(
                equipment.getEquipmentId(),
                equipment.getEquipmentName(),
                equipment.getDescription(),
                equipment.getTotalQuantity()
        );
    }
}
