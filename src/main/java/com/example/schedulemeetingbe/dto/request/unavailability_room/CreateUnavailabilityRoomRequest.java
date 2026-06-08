package com.example.schedulemeetingbe.dto.request.unavailability_room;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUnavailabilityRoomRequest(
        @NotNull(message = StringCommon.NOT_NULL + "roomId")
        Long roomId,

        @NotBlank(message = "Dữ liệu reason " + StringCommon.NOT_BLANK)
        String reason,

        @NotNull(message = StringCommon.NOT_NULL + "startTime")
        Long startTime,

        @NotNull(message = StringCommon.NOT_NULL + "endTime")
        Long endTime
) {
}
