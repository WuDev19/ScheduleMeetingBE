package com.example.schedulemeetingbe.dto.response.booking.booking_overlap;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.Instant;

public interface BookingOverlapProjection {

    Long getBookingId();

    String getTitle();

    String getUserBooked();

    String getPhone();

    String getRoomName();

    BookingStatus getStatus();

    Instant getStartTime();

    Instant getEndTime();
}
