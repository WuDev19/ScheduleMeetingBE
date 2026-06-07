package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.constant.StringCommon;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private TimeUtils() {
    }

    public static ZonedDateTime fromLongToZoneDateTime(Long time) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC);
    }

    public static String dateTimeFormat() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
    }


}
