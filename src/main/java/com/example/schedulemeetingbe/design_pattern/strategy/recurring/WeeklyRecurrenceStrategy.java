package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.exception.custom_exception.OverlapBookingException;
import com.example.schedulemeetingbe.helper.CreatePayloadHelper;
import com.example.schedulemeetingbe.helper.RecurrenceHelper;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
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
    private final BookingHistoryRepository bookingHistoryRepository;
    private final JsonMapper jsonMapper;

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
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        long gapInMinutes = ChronoUnit.MINUTES.between(request.meetingStartTime(), request.meetingEndTime());
        List<Booking> bookings = new ArrayList<>();
        List<BookingHistory> bookingHistories = new ArrayList<>();
        List<String> rangesTime = new ArrayList<>();
        Set<DayOfWeek> days = Arrays.stream(request.dayOfWeeks().split(","))
                .map(String::trim)
                .map(DayOfWeek::valueOf)
                .collect(Collectors.toSet());
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            LocalDate weekStart =
                    currentDate.with(DayOfWeek.MONDAY);
            for (DayOfWeek day : days) {
                LocalDate bookingDate =
                        weekStart.with(day);
                if (bookingDate.isBefore(startDate)) {
                    continue;
                }
                if (bookingDate.isAfter(endDate)) {
                    continue;
                }
                ZonedDateTime startTime = ZonedDateTime.of(
                        bookingDate,
                        request.meetingStartTime(),
                        TimeUtils.ZONE_ID
                );
                ZonedDateTime endTime = startTime.plusMinutes(gapInMinutes);
                bookings.add(Booking.builder()
                        .room(room)
                        .bookedBy(register)
                        .startTime(startTime)
                        .endTime(endTime)
                        .recurringPattern(recurringPattern)
                        .build()
                );
                String rangeStr = String.format("[%s, %s)", startTime.toOffsetDateTime(), endTime.toOffsetDateTime());
                rangesTime.add(rangeStr);
            }
            currentDate = currentDate.plusWeeks(interval);
        }

//            while (!startDate.isAfter(endDate)) {
//                if (days.contains(startDate.getDayOfWeek())) {
//                    String rangeStr = String.format("[%s, %s)", startTime.toOffsetDateTime(), endTime.toOffsetDateTime());
//                    rangesTime.add(rangeStr);
//                    Booking booking = Booking.builder()
//                            .room(room)
//                            .bookedBy(register)
//                            .startTime(startTime)
//                            .endTime(endTime)
//                            .recurringPattern(recurringPattern)
//                            .build();
//                    bookings.add(booking);
//                }
//                if (startDate.getDayOfWeek() == DayOfWeek.SUNDAY) {
//                    startDate = startDate.plusWeeks(interval);
//                    startTime = startTime.plusWeeks(interval);
//                }
//                startDate = startDate.plusDays(1);
//                startTime = startTime.plusDays(1);
//                endTime = startTime.plusMinutes(gapInMinutes);
//            }
        List<String> reasons = bookingRepository.checkOverlap(room.getRoomId(), rangesTime.toArray(new String[0]));
        if (!reasons.isEmpty()) {
            throw new OverlapBookingException(reasons);
        }
        List<Booking> bookingList = bookingRepository.saveAll(bookings);
        bookingList.forEach(booking -> {
            UpdateBookingChangePayload payload = CreatePayloadHelper.create(
                    booking,
                    register.getUserId(),
                    room.getRoomId()
            );
            BookingHistory bookingHistory = BookingHistory.builder()
                    .booking(booking)
                    .actionType(BookingActionType.CREATED)
                    .changedBy(register)
                    .newData(jsonMapper.valueToTree(payload))
                    .build();
            bookingHistories.add(bookingHistory);
        });
        bookingHistoryRepository.saveAll(bookingHistories);
    }
}
