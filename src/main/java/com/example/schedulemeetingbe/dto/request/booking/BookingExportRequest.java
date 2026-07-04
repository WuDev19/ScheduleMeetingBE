package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingExportType;

public record BookingExportRequest(
        BookingExportType exportType
) {
}
