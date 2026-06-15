package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import com.example.schedulemeetingbe.repository.RecurringPatternRepository;
import com.example.schedulemeetingbe.service.base.IRecurringPatternService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RecurringPatternServiceImpl implements IRecurringPatternService {

    private final RecurringPatternRepository recurringPatternRepository;

    @Override
    public RecurringPatternResponse createForAPI(RecurringPatternCreateRequest request) {
        /*
        if (recurringPatternRequest != null && request.equipments() == null) {
            RecurringPattern recurringPattern = RecurringPatternMapper.mapToRecurringPattern(recurringPatternRequest, user);
            RecurringPattern recurSaved = iRecurringPatternService.save(recurringPattern);
            int interval = recurringPatternRequest.interval();
            List<Booking> bookings = new LinkedList<>();
            Set<DayOfWeek> days = Arrays.stream(recurringPatternRequest.dayOfWeeks().split(","))
                    .map(String::trim)
                    .map(DayOfWeek::valueOf)
                    .collect(Collectors.toSet()); //lấy kiểu ngày trong tuần để check trong vòng lặp
            ZonedDateTime endDate = recurringPatternRequest.endDate()
                    .atTime(LocalTime.MAX)
                    .atZone(request.start().getZone()); //map sang zoned date time
            ZonedDateTime currentStart = request.start();
            ZonedDateTime currentEnd = request.end();
            long gapInMinutes = ChronoUnit.MINUTES.between(currentStart, currentEnd); //tính thời gian chuẩn giữa start với end
            List<String> rangeStrings = new ArrayList<>();
            while (!request.start().isAfter(endDate)) {
                if (days.contains(currentStart.getDayOfWeek())) {
                    String rangeStr = String.format("[%s, %s)", currentStart.toOffsetDateTime(), currentEnd.toOffsetDateTime());
                    rangeStrings.add(rangeStr);
                    Booking booking = Booking.builder()
                            .room(room)
                            .bookedBy(user)
                            .startTime(currentStart)
                            .endTime(currentEnd)
                            .recurringPattern(recurSaved)
                            .build();
                    bookings.add(booking);
                }
                currentStart = currentStart.plusDays(interval - 1);
                currentEnd = currentStart.plusMinutes(gapInMinutes);
            }
            Booking firstBookingApply = bookings.get(0);
            firstBookingApply.setTitle(request.title());
            firstBookingApply.setDescription(request.description());
            firstBookingApply.setAttendeeCount(request.attendee());
            bookingRepository.checkOverlap(
                    request.roomId(),
                    rangeStrings.toArray(new String[0])
            ).ifPresent(reason -> {
                throw new CheckOverlapBookingException(reason);
            });
            List<Booking> bookingSaved = bookingRepository.saveAll(bookings);
            return Map.of("bookingId", bookingSaved.stream().map(Booking::getBookingId).toList());
        }
        */
        return null;
    }

    @Override
    public RecurringPattern save(RecurringPattern recurringPattern) {
        return recurringPatternRepository.save(recurringPattern);
    }
}
