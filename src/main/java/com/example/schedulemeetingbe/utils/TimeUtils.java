package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.constant.StringCommon;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static ZonedDateTime ZONE_DATE_TIME = ZonedDateTime.now(ZoneOffset.UTC);

    private TimeUtils() {
    }

    public static ZonedDateTime fromLongToZoneDateTime(Long time) {
        return time != null ? ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneOffset.UTC) : null;
    }

    public static String dateTimeFormat() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
    }


}
