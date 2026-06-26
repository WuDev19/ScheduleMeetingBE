package com.example.schedulemeetingbe.schedule.schedule;

import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.schedule.process.OutboxEventRemindProcess;
import com.example.schedulemeetingbe.service.base.IBookingService;
import com.example.schedulemeetingbe.service.base.IRedisService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventRemind {

    private final IBookingService iBookingService;
    private final IRedisService iRedisService;
    private final OutboxEventRemindProcess outboxEventRemindProcess;

    @Scheduled(fixedDelay = 12000)
    public void process() {
        List<Booking> bookings = iBookingService.getApprovedBooking();
        bookings.forEach(booking -> {
            long timeRemain = ChronoUnit.MINUTES.between(TimeUtils.now(), booking.getStartTime());
            if (timeRemain <= 30 && timeRemain >= 0) {
                Boolean set = iRedisService.setIfAbsent(booking.getBookingId().toString(), "Sent", Duration.ofMinutes(15));
                if (Boolean.TRUE.equals(set)){
                    outboxEventRemindProcess.process(booking.getBookingId(), timeRemain);
                }
            }
        });
    }
}
