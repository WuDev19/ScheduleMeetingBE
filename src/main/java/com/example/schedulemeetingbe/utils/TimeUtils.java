package com.example.schedulemeetingbe.utils;

import com.example.schedulemeetingbe.constant.StringCommon;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class TimeUtils {
    private TimeUtils(){}

    public static LocalDate fromLongToLocalDate(Long time){
        return LocalDate.ofInstant(Instant.ofEpochMilli(time), ZoneId.of(StringCommon.TIME_ZONE_VN));
    }

    public static String dateTimeFormat(){
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
    }
}
