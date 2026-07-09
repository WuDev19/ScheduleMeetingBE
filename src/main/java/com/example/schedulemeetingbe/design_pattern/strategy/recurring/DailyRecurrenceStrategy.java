package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.helper.RecurrenceHelper;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.service.base.IRoomService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRecurrenceStrategy implements RecurrencePatternStrategy {
    private final BookingRepository bookingRepository;
    private final IRoomService iRoomService;

    @Override
    public RecurrenceType getType() {
        return RecurrenceType.DAILY;
    }

    @Override
    public void create(RecurringPattern recurringPattern, RecurringPatternCreateRequest request, User register, Room room) {
        if (RecurrenceHelper.checkLimitRecurrence(request.startDate(), request.endDate())) {
            throw new BusinessException(ErrorResponse.EXCEED_PERIODIC);
        }
        int interval = request.interval() != null ? request.interval() : 1;
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        OffsetDateTime startTime = OffsetDateTime.of(startDate, request.meetingStartTime(), TimeUtils.ZONE_OFFSET);
        OffsetDateTime endTime = OffsetDateTime.of(startDate, request.meetingEndTime(), TimeUtils.ZONE_OFFSET);
        long gapInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);

        List<Booking> bookings = new ArrayList<>();
        List<String> rangesTime = new ArrayList<>();
        List<OffsetDateTime> dateTimesForLock = new ArrayList<>();
        // hiện tại thì sẽ cho đặt lịch họp hôm thứ 7, chủ nhật,
        // sau tùy theo logic thì có thể chỉ cho họp trong ngày giờ hành chính
        while (!startDate.isAfter(endDate)) {
            Booking booking = Booking.builder()
                    .title(request.title())
                    .description(request.description())
                    .attendeeCount(request.attendeeCount() != null ? request.attendeeCount() : 1)
                    .bookedBy(register)
                    .room(room)
                    .startTime(startTime)
                    .endTime(endTime)
                    .recurringPattern(recurringPattern)
                    .build();
            bookings.add(booking);
            dateTimesForLock.add(startTime);
            String rangeStr = String.format("[%s, %s)", startTime, endTime);
            rangesTime.add(rangeStr);
            startDate = startDate.plusDays(interval);
            startTime = startTime.plusDays(interval);
            endTime = startTime.plusMinutes(gapInMinutes);
        }
        RecurrenceHelper.validateAndSaveBooking(request.roomId(), bookings, rangesTime, bookingRepository, iRoomService, dateTimesForLock);
    }
}
