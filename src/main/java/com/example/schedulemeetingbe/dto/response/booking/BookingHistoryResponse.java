package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;

import java.time.ZonedDateTime;

public record BookingHistoryResponse(
        Long bookingId,
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
        Long bookingHistoryId,
        String userChange,
        BookingActionType actionType,
        Object oldData,
        Object newData
) {
}
