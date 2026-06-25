package com.example.schedulemeetingbe.dto.response.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.OffsetDateTime;

public record StatusBookingResponse(
        Long bookingId,
        BookingStatus status,
        @JsonFormat(pattern = StringCommon.DATE_TIME_FORMAT_NO_TZ,
                timezone = StringCommon.TIME_ZONE_VN)
        OffsetDateTime updatedAt
) {
}
