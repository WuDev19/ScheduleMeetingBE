package com.example.schedulemeetingbe.dto.request.room;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

public record StartEndTimeRequest (
        @NotNull(message = StringCommon.NOT_NULL + "start")
        @DateTimeFormat(pattern = StringCommon.OFFSET_FORMAT) //cái này dùng cho query params
        OffsetDateTime start,

        @NotNull(message = StringCommon.NOT_NULL + "end")
        @DateTimeFormat(pattern = StringCommon.OFFSET_FORMAT)
        OffsetDateTime end
) {
}
