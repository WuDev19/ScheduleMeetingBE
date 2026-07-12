package com.example.schedulemeetingbe.utils;

import java.time.OffsetDateTime;
import java.util.List;

public class LockKeyUtils {
    private LockKeyUtils() {}

    public static long createKey(Long roomId, OffsetDateTime dateTime){
        int dateAsInt = dateTime.getYear() * 10000 + dateTime.getMonthValue() * 100 + dateTime.getDayOfMonth();
        return roomId * 100000000L + dateAsInt;
    }

    public static long[] forRoomAndDates(Long roomId, List<OffsetDateTime> dates) {
        return dates.stream()
                .mapToLong(date -> createKey(roomId, date))
                .sorted()
                .toArray();
    }
}
