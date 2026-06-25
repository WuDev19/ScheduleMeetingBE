package com.example.schedulemeetingbe.dto.response.booking;

import java.time.OffsetDateTime;
import java.util.List;

public record BookingDetailResponse(
        Long id,
        String title,
        String description,
        Long roomId,
        String roomName,
        String roomAddress,
        Integer floorNumber,
        String userBooked,
        String phone,
        String email,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer attendee,
        OffsetDateTime createdAt,
        List<BookingDetailEquipmentResponse> equipments
) {
}