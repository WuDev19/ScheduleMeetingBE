package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.constant.enums.EventType;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.CreateBookingPayload;
import com.example.schedulemeetingbe.entity.payload.ReceiverEmailPayload;
import com.example.schedulemeetingbe.repository.BookingHistoryRepository;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class CreatedApproveCommand extends BookingApproveCommand {

    public CreatedApproveCommand(
            INotificationService iNotificationService,
            OutboxEventRepository outboxEventRepository,
            BookingHistoryRepository bookingHistoryRepository,
            JsonMapper jsonMapper
    ) {
        super(iNotificationService, outboxEventRepository, bookingHistoryRepository, jsonMapper);
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.CREATED;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        super.execute(booking, request, approver);
        CreateBookingPayload payload = jsonMapper.treeToValue(request.newData(), CreateBookingPayload.class);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.now());
        createOutboxEvent(payload.emails(), booking, booking.getRoom());
    }

    //gửi thông báo/email về lịch họp cho người tham dự
    private void createOutboxEvent(List<String> receivers, Booking booking, Room room) {
        Building building = room.getBuilding();
        ReceiverEmailPayload payload = new ReceiverEmailPayload(
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
                .eventType(EventType.SEND_EMAIL_CONFIRM_PARTICIPATE.name())
                .payload(jsonMapper.valueToTree(payload))
                .build();
        outboxEventRepository.save(event);
    }

}
