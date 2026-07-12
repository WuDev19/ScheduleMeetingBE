package com.example.schedulemeetingbe.dto.response.recurrence;

import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.RecurrenceType;
import com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Builder(access = AccessLevel.PUBLIC)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RecurringPatternResponse {
    private Long recurringId;
    private Integer interval;
    private String daysOfWeek;
    private LocalDate startDate;
    private LocalDate endDate;
    private BookingStatus status;
    private Long userCreatedId;
    private String userCreatedName;
    private List<BookingRecurrenceResponse> bookings;
    private RecurrenceType recurrenceType;
}
