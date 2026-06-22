package com.example.schedulemeetingbe.design_pattern.strategy.recurring;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UpdateBookingChangePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.helper.CreatePayloadHelper;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DailyRecurrenceStrategy implements RecurrencePatternStrategy {
    private final BookingRepository bookingRepository;
    private final BookingHistoryRepository bookingHistoryRepository;
    private final JsonMapper jsonMapper;

    @Override
    public RecurrenceType getType() {
        return RecurrenceType.DAILY;
    }

    @Override
    public void create(RecurringPattern recurringPattern, RecurringPatternCreateRequest request, User register, Room room) {
        if(ChronoUnit.DAYS.between(request.startDate(), request.endDate()) >= 30){
            throw new BusinessException(ErrorResponse.EXCEED_PERIODIC);
        }
        int interval = request.interval() != null ? request.interval() : 1;
        LocalDate startDate = request.startDate();
        LocalDate endDate = request.endDate();
        ZonedDateTime startTime = ZonedDateTime.of(startDate, request.meetingStartTime(), TimeUtils.ZONE_ID);
        ZonedDateTime endTime = ZonedDateTime.of(startDate, request.meetingEndTime(), TimeUtils.ZONE_ID);
        long gapInMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
        List<Booking> bookings = new ArrayList<>();
        List<BookingHistory> bookingHistories = new ArrayList<>();
        // hiện tại thì sẽ cho đặt lịch họp hôm thứ 7, chủ nhật,
        // sau tùy theo logic thì có thể chỉ cho họp trong ngày giờ hành chính
        while (!startDate.isAfter(endDate)) {
            Booking booking = Booking.builder()
                    .bookedBy(register)
                    .room(room)
                    .startTime(startTime)
                    .endTime(endTime)
                    .recurringPattern(recurringPattern)
                    .build();
            bookings.add(booking);
            startDate = startDate.plusDays(interval);
            startTime = startTime.plusDays(interval);
            endTime = startTime.plusMinutes(gapInMinutes);
        }
        List<Booking> bookingList = bookingRepository.saveAll(bookings);

        //lưu booking_history
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
