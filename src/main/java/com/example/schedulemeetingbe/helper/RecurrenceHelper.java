package com.example.schedulemeetingbe.helper;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RecurrenceHelper {
    private RecurrenceHelper(){}

    public static boolean checkLimitRecurrence(LocalDate start, LocalDate end){
        return ChronoUnit.DAYS.between(start, end) >= 30;
    }
}
