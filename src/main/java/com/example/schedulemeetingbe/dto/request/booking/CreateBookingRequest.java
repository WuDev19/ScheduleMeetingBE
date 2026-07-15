package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;
import java.util.List;

public record CreateBookingRequest(
        @NotNull(message = StringCommon.NOT_NULL + "roomId")
        Long roomId,

        @NotBlank(message = "Dữ liệu title " + StringCommon.NOT_BLANK)
        String title,

        String description,

        @NotNull(message = StringCommon.NOT_NULL + "start")
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime start,

        @NotNull(message = StringCommon.NOT_NULL + "end")
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime end,

        List<CreateBookingEquipmentRequest> equipments,

        List<String> receivers,

        Long departmentId
) {
}
