package com.example.schedulemeetingbe.dto.response.booking;

public record BookingDetailEquipmentResponse(
        Long bookingEquipmentId,
        Long equipmentId,
        String equipmentName,
        String description,
        Integer usingQuantity
) {
}
