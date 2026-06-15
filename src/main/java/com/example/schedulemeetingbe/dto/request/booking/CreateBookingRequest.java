package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;
import java.util.List;

public record CreateBookingRequest(
        @NotNull(message = StringCommon.NOT_NULL + "roomId")
        Long roomId,

        @NotNull(message = StringCommon.NOT_NULL + "userId")
        Long userId,

        @NotBlank(message = "Dữ liệu title " + StringCommon.NOT_BLANK)
        String title,

        String description,

        @NotNull(message = StringCommon.NOT_NULL + "start")
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime start,

        @NotNull(message = StringCommon.NOT_NULL + "end")
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime end,

        @NotNull(message = StringCommon.NOT_NULL + "attendee")
        Integer attendee,

        List<CreateBookingEquipmentRequest> equipments
) {
}
