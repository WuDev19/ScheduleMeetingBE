package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.enums.BookingExportType;
import jakarta.validation.constraints.NotNull;

public record BookingExportRequest(
        @NotNull(message = "Dữ liệu viewType " + "không được để trống")
        BookingExportType exportType
) {
}
