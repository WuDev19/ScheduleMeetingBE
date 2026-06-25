package com.example.schedulemeetingbe.entity.payload;

public record UpdateBookingEquipmentQuantityPayload(
        Long beId,
        Integer quantity
) {
}
