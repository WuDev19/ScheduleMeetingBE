package com.example.schedulemeetingbe.entity.payload;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingEquipmentRequest;

import java.time.OffsetDateTime;
import java.util.List;

// đang để record để tối ưu bộ nhớ tuy nhiên sẽ ko tái sử dụng field đc, nào có time sẽ xem xét lại kế thừa 1 lớp base
public record CreateBookingPayload (
        Long bookingId,
        String title,
        String description,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        Integer attendeeCount,
        BookingStatus status,
        String cancellationReason,
        Long roomId,
        Long bookedBy,
        OffsetDateTime createdAt,
        List<String> emails,
        List<CreateBookingEquipmentRequest> equipments
) {
}
