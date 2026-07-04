package com.example.schedulemeetingbe.dto.response.booking.booking_summary;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.Instant;

public interface BookingSummaryProjection {

    Long getBookingId();

    Long getHistoryId();

    String getTitle();

    String getUserBooked();

    String getPhone();

    String getRoomName();

    BookingStatus getStatus();

    BookingActionType getActionType();

    Instant getStartTime();

    Instant getEndTime();

}
