package com.example.schedulemeetingbe.schedule.process;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.Room;
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
        iBookingService.getBooking(bookingId).ifPresent(booking -> {
            Room room = booking.getRoom();

            //gửi thông báo cho những người đã xác nhân tham gia
            List<User> users = iBookingAttendeeService.getAttendOfBooking(bookingId);
            List<Notification> notifications = new ArrayList<>();
            List<String> emails = new ArrayList<>();
            String message = "Cuộc họp " +
                    booking.getTitle() +
                    " tại phòng " +
                    room.getRoomName() +
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
                    booking.getTitle(),
                    room.getRoomName(),
                    minutes
            );
            iEmailService.sendEmailRemainingBooking(payload);
        });
    }
}
