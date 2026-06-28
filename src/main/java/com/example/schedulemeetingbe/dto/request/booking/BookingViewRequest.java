package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingViewType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record BookingViewRequest(
        BookingViewType viewType,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate targetDate,
        String fullName
) {
}
