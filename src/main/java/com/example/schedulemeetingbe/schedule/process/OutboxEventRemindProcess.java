package com.example.schedulemeetingbe.schedule.process;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.RemainingBookingPayload;
import com.example.schedulemeetingbe.service.base.IBookingAttendeeService;
import com.example.schedulemeetingbe.service.base.IBookingService;
import com.example.schedulemeetingbe.service.base.IEmailService;
import com.example.schedulemeetingbe.service.base.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventRemindProcess {

    private final IBookingService iBookingService;
    private final IBookingAttendeeService iBookingAttendeeService;
    private final INotificationService iNotificationService;
    private final IEmailService iEmailService;

    @Async("outboxExecutor")
    public void process(Long bookingId, long minutes) {
        iBookingService.getBookingReminding(bookingId).ifPresent(bookingRemain -> {

            //gửi thông báo cho những người đã xác nhận tham gia
            Booking booking = iBookingService.getBooking(bookingId).orElse(null);
            List<User> users = iBookingAttendeeService.getAttendOfBooking(bookingId);
            List<Notification> notifications = new ArrayList<>();
            List<String> emails = new ArrayList<>();
            String message = "Cuộc họp " +
                    bookingRemain.title() +
                    " tại phòng " +
                    bookingRemain.roomName() +
                    " còn " +
                    minutes +
                    " sẽ diễn ra";
            users.forEach(user -> {
                emails.add(user.getEmail());
                Notification notification = Notification.builder()
                        .title(StringCommon.TITLE_NOTIFICATION)
                        .message(message)
                        .user(user)
                        .booking(booking)
                        .build();
                notifications.add(notification);
            });
            iNotificationService.save(notifications);
            RemainingBookingPayload payload = new RemainingBookingPayload(
                    emails,
                    bookingRemain.title(),
                    bookingRemain.roomName(),
                    minutes
            );
            iEmailService.sendEmailRemainingBooking(payload);
        });
    }
}
