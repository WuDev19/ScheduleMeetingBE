package com.example.schedulemeetingbe.dto.response.booking.booking_notification;

import com.example.schedulemeetingbe.entity.User;

public record BookingAndUserResponse(
        Long bookingId,
        User user
) {
}
