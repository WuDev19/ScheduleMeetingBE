package com.example.schedulemeetingbe.dto.response.booking;

import java.time.ZonedDateTime;
import java.util.List;

public record BookingDetailResponse(
        Long id,
        String title,
        String description,
        String roomName,
        String roomAddress,
        Integer floorNumber,
        String userBooked,
        String phone,
        String email,
        ZonedDateTime startTime,
        ZonedDateTime endTime,
        Integer attendee,
        ZonedDateTime createdAt,
        List<BookingDetailEquipmentResponse> equipments
) {
}