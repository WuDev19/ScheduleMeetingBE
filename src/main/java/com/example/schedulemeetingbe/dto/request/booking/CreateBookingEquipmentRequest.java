package com.example.schedulemeetingbe.dto.request.booking;

public record CreateBookingEquipmentRequest(
        Long equipmentId,
        Integer quantity
) {
}
