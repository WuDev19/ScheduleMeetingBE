package com.example.schedulemeetingbe.dto.request.room;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record StartEndTimeRequest (
        @NotNull(message = StringCommon.NOT_NULL + "start")
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime start,

        @NotNull(message = StringCommon.NOT_NULL + "end")
        @JsonFormat(
                pattern = StringCommon.OFFSET_FORMAT
        )
        OffsetDateTime end
) {
}
