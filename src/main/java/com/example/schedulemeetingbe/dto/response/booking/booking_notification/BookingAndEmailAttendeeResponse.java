package com.example.schedulemeetingbe.dto.response.booking.booking_notification;

public record BookingAndEmailAttendeeResponse(
        Long bookingId,
        String email
) {
}
