package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotBlank;

public record CancelBookingRequest(
        @NotBlank(message = "Dữ liệu roomName " + StringCommon.NOT_BLANK)
        String reason
) {
}
