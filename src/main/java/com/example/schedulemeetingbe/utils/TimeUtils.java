package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.constant.StringCommon;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static final ZoneId ZONE_ID = ZoneId.of(StringCommon.TIME_ZONE_VN);
    public static final ZoneOffset ZONE_OFFSET = ZoneOffset.of("+07:00");

    private TimeUtils() {
    }

    public static OffsetDateTime now() {
        return OffsetDateTime.now(ZONE_ID);
    }

    public static String dateTimeFormat() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ));
    }

    public static OffsetDateTime parseOffsetDateTime(String dateTime) {
        if (dateTime == null || dateTime.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(dateTime, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (java.time.format.DateTimeParseException ex) {
            try {
                return OffsetDateTime.parse(dateTime, DateTimeFormatter.ofPattern(StringCommon.OFFSET_FORMAT));
            } catch (java.time.format.DateTimeParseException e) {
                try {
                    return OffsetDateTime.parse(dateTime + ZONE_OFFSET.toString(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                } catch (java.time.format.DateTimeParseException ignore) {
                    throw new RuntimeException("Could not parse OffsetDateTime: " + dateTime);
                }
            }
        }
    }
}
