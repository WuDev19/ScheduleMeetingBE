package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.ApproveRejectRecurrencePayload;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
public abstract class BookingRollbackCommand {

    protected final INotificationService iNotificationService;
    protected final OutboxEventRepository outboxEventRepository;
    protected final JsonMapper jsonMapper;

    abstract BookingActionType getActionType();

    @Transactional
    public void execute(
            Booking booking,
            RollBackRequest request,
            User approver
    ) {

        StringBuilder message = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ);
        String startTimeStr = booking.getStartTime().format(formatter);
        String endTimeStr = booking.getEndTime().format(formatter);
        String timeRange = "từ " + startTimeStr + " đến " + endTimeStr;
        String roomName = booking.getRoom().getRoomName();
        User bookedBy = booking.getBookedBy();
        switch (request.actionType()) {
            case CREATED -> message.append("Yêu cầu tạo lịch họp mới \"").append(booking.getTitle()).append("\" ")
                    .append(timeRange).append(" tại ").append(roomName)
                    .append(" đã bị từ chối.");
            case UPDATED ->
                    message.append("Yêu cầu thay đổi thông tin lịch họp \"").append(booking.getTitle()).append("\" ")
                            .append(timeRange).append(" tại ").append(roomName)
                            .append(" đã bị từ chối.");
            case ADD_EQUIPMENT ->
                    message.append("Yêu cầu cấp thêm thiết bị cho lịch họp \"").append(booking.getTitle()).append("\" ")
                            .append(timeRange).append(" tại ").append(roomName)
                            .append(" đã bị từ chối.");
            case UPDATE_EQUIP_QUANTITY ->
                    message.append("Yêu cầu thay đổi số lượng thiết bị của lịch họp \"").append(booking.getTitle()).append("\" ")
                            .append(timeRange).append(" đã bị từ chối.");
        }

        if (request.reason() != null && !request.reason().isBlank()) {
            message.append(" Lý do: ").append(request.reason());
        }

        Notification notification = iNotificationService.save(
                StringCommon.TITLE_NOTIFICATION_EMAIL,
                message.toString(),
                bookedBy,
                booking);
        ApproveRejectRecurrencePayload payload = new ApproveRejectRecurrencePayload(
                bookedBy.getEmail(),
                notification.getTitle(),
                notification.getMessage()
        );
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .payload(jsonMapper.valueToTree(payload))
                .eventType(EVENT_TYPE.SEND_EMAIL_APPROVE_REJECT.name())
                .status(OutboxStatus.PENDING)
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
