package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
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
        String username,
        String phone,
        String email,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer attendee,
        OffsetDateTime createdAt,
        List<BookingDetailEquipmentResponse> equipments,
        BookingStatus status
) {
}