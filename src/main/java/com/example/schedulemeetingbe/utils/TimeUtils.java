package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.constant.StringCommon;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static final ZoneId ZONE_ID = ZoneId.of(StringCommon.TIME_ZONE_VN);
    public static ZonedDateTime ZONE_DATE_TIME = ZonedDateTime.now(ZONE_ID);

    private TimeUtils() {
    }

    public static ZonedDateTime fromLongToZoneDateTime(Long time) {
        return time != null ? ZonedDateTime.ofInstant(Instant.ofEpochMilli(time), ZONE_ID) : null;
    }

    public static String dateTimeFormat() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
    }

}
