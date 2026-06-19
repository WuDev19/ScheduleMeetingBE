package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;

import java.time.Instant;
import java.time.ZonedDateTime;

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
