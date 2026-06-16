package com.example.schedulemeetingbe.dto.request.room;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record StartEndTimeRequest (
        @NotNull(message = StringCommon.NOT_NULL + "start")
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime start,

        @NotNull(message = StringCommon.NOT_NULL + "end")
        @JsonFormat(
                pattern = "yyyy-MM-dd HH:mm:ssX"
        )
        ZonedDateTime end
) {
}
