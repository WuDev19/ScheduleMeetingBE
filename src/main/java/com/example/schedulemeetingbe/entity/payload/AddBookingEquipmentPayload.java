package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.dto.response.booking.BookingDetailEquipmentResponse;

import java.util.List;

public record AddBookingEquipmentPayload(
        Long bookingId,
        List<BookingDetailEquipmentResponse> equipments
) {
}
