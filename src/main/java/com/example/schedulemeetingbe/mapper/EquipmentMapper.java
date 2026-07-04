package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.equipment.EquipmentResponse;
import com.example.schedulemeetingbe.dto.response.equipment.RoomEquipmentResponse;
import com.example.schedulemeetingbe.entity.Equipment;
import com.example.schedulemeetingbe.entity.RoomEquipment;

public final class EquipmentMapper {
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
