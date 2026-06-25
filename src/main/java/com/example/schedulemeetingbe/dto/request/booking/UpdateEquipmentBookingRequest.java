package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingEquipmentAction;

public record UpdateEquipmentBookingRequest(
        Long equipmentId,
        Integer quantity,
        BookingEquipmentAction action
) {
}
