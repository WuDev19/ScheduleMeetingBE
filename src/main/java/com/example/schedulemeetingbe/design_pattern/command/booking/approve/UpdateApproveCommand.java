package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.*;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.UpdateApprovePayload;
import com.example.schedulemeetingbe.entity.payload.UpdateFocusRoomOrTimePayload;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.BookingReservationRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class UpdateApproveCommand extends BookingApproveCommand {
    private final BookingReservationRepository bookingReservationRepository;

    public UpdateApproveCommand(INotificationService iNotificationService,
                                OutboxEventRepository outboxEventRepository,
                                BookingHistoryRepository bookingHistoryRepository,
                                JsonMapper jsonMapper,
                                BookingReservationRepository bookingReservationRepository
    ) {
        super(iNotificationService, outboxEventRepository, bookingHistoryRepository, jsonMapper);
        this.bookingReservationRepository = bookingReservationRepository;
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.UPDATED;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        super.execute(booking, request, approver);
        UpdateFocusRoomOrTimePayload payload = jsonMapper.treeToValue(request.newData(), UpdateFocusRoomOrTimePayload.class);
        createOutboxEvent(payload.getEmails(), booking, booking.getRoom());
        bookingReservationRepository.findBookingReservationsByBooking_BookingId(booking.getBookingId())
                .ifPresent(bookingReservation ->
                        bookingReservation.setStatus(ReservationStatus.DONE)
                );
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.now());
    }

    private void createOutboxEvent(List<String> receivers, Booking booking, Room room) {
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
    }
}
