package com.example.schedulemeetingbe.dto.request.unavailability_room;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record CreateUnavailabilityRoomRequest(
        @NotNull(message = StringCommon.NOT_NULL + "roomId")
        Long roomId,

        @NotBlank(message = "Dữ liệu reason " + StringCommon.NOT_BLANK)
        String reason,

        @NotNull(message = StringCommon.NOT_NULL + "startTime")
        @JsonFormat(pattern = StringCommon.OFFSET_FORMAT)
        OffsetDateTime startTime,

        @NotNull(message = StringCommon.NOT_NULL + "endTime")
        @JsonFormat(pattern = StringCommon.OFFSET_FORMAT)
        OffsetDateTime endTime
) {
}
