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

}
