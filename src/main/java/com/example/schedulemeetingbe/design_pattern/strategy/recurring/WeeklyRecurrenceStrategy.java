package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.helper.RecurrenceHelper;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class WeeklyRecurrenceStrategy implements RecurrencePatternStrategy {
    private final BookingRepository bookingRepository;

    @Override
    public RecurrenceType getType() {
        return RecurrenceType.WEEKLY;
    }

    @Override
    public void create(RecurringPattern recurringPattern, RecurringPatternCreateRequest request, User register, Room room) {
        if (RecurrenceHelper.checkLimitRecurrence(request.startDate(), request.endDate())) {
            throw new BusinessException(ErrorResponse.EXCEED_PERIODIC);
        }
        int interval = request.interval() != null ? request.interval() : 1;
        List<Booking> bookings = new ArrayList<>();
        List<String> rangesTime = new ArrayList<>();
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        long gapInMinutes = ChronoUnit.MINUTES.between(request.meetingStartTime(), request.meetingEndTime());
        Set<DayOfWeek> days = Arrays.stream(request.dayOfWeeks().split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
        while (!startDate.isAfter(endDate)) {
            LocalDate weekStart =
                    startDate.with(DayOfWeek.MONDAY);
            for (DayOfWeek day : days) {
                LocalDate bookingDate =
                        weekStart.with(day);
                //fix ở đây
                if (bookingDate.isBefore(startDate)) {
                    continue;
                }
                if (bookingDate.isAfter(endDate)) {
                    continue;
                }
                OffsetDateTime startTime = OffsetDateTime.of(
                        bookingDate,
                        request.meetingStartTime(),
                        TimeUtils.ZONE_OFFSET
                );
                OffsetDateTime endTime = startTime.plusMinutes(gapInMinutes);
                bookings.add(Booking.builder()
                        .room(room)
                        .bookedBy(register)
                        .startTime(startTime)
                        .endTime(endTime)
                        .recurringPattern(recurringPattern)
                        .build()
                );
                String rangeStr = String.format("[%s, %s)", startTime, endTime);
                rangesTime.add(rangeStr);
            }
            startDate = startDate.plusWeeks(interval);
        }
        RecurrenceHelper.validateAndSaveBooking(room, bookings, rangesTime, bookingRepository);
    }

}
