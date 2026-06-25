package com.example.schedulemeetingbe.dto.request.booking;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record RecurringPatternCreateRequest(
        @NotNull(message = StringCommon.NOT_NULL + "type")
        RecurrenceType type,

        Integer interval,

        String dayOfWeeks,

        @NotNull(message = StringCommon.NOT_NULL + "startDate")
        LocalDate startDate,

        @NotNull(message = StringCommon.NOT_NULL + "endDate")
        LocalDate endDate,

        @NotNull(message = StringCommon.NOT_NULL + "meetingStartTime")
        LocalTime meetingStartTime,

        @NotNull(message = StringCommon.NOT_NULL + "meetingEndTime")
        LocalTime meetingEndTime,

        @NotNull(message = StringCommon.NOT_NULL + "roomId")
        Long roomId
) {
}
