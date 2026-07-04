package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.*;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UpdateApprovePayload;
import com.example.schedulemeetingbe.entity.payload.UpdateFocusRoomOrTimePayload;
import com.example.schedulemeetingbe.repository.BookingAttendeeRepository;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.BookingReservationRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateApproveCommand extends BookingApproveCommand {
    private final BookingReservationRepository bookingReservationRepository;
    private final BookingAttendeeRepository bookingAttendeeRepository;

    public UpdateApproveCommand(INotificationService iNotificationService,
                                OutboxEventRepository outboxEventRepository,
                                BookingHistoryRepository bookingHistoryRepository,
                                JsonMapper jsonMapper,
                                BookingReservationRepository bookingReservationRepository,
                                BookingAttendeeRepository bookingAttendeeRepository
    ) {
        super(iNotificationService, outboxEventRepository, bookingHistoryRepository, jsonMapper);
        this.bookingReservationRepository = bookingReservationRepository;
        this.bookingAttendeeRepository = bookingAttendeeRepository;
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATED;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        super.execute(booking, request, approver);
        UpdateFocusRoomOrTimePayload payload = jsonMapper.treeToValue(request.newData(), UpdateFocusRoomOrTimePayload.class);
        createNotificationAndOutboxEvent(payload.getEmails(), booking, booking.getRoom());
        bookingReservationRepository.findBookingReservationsByBooking_BookingId(booking.getBookingId())
                .ifPresent(bookingReservation ->
                        bookingReservation.setStatus(ReservationStatus.DONE)
                );
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.now());
    }

    //gửi thông báo web và email
    private void createNotificationAndOutboxEvent(List<String> receivers, Booking booking, Room room) {
        Building building = room.getBuilding();
        UpdateApprovePayload payload = new UpdateApprovePayload(
                booking.getBookingId(),
                booking.getTitle(),
                booking.getDescription(),
                "Tòa nhà " + building.getBuildingName() + ", " + building.getAddress(),
                "Tầng " + room.getFloorNumber() + ", phòng " + room.getRoomName(),
                booking.getStartTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                booking.getEndTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ)),
                receivers
        );
        OutboxEvent event = OutboxEvent.builder()
                .status(OutboxStatus.PENDING)
                .eventType(EventType.SEND_EMAIL_APPROVE_UPDATE.name())
                .payload(jsonMapper.valueToTree(payload))
                .build();
        outboxEventRepository.save(event);

        List<User> users = bookingAttendeeRepository.getAttendeeOfBooking(booking.getBookingId());
        List<Notification> notifications = new ArrayList<>();
        users.forEach(user -> {
            Notification notification = Notification.builder()
                    .title(StringCommon.TITLE_NOTIFICATION)
                    .booking(booking)
                    .message("Lịch họp có sự thay đổi về phòng hoặc thời gian họp, bấm để xem thêm chi tiết")
                    .user(user)
                    .build();
            notifications.add(notification);
        });
        iNotificationService.save(notifications);
    }
}
