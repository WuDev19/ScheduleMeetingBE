package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
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
public abstract class BookingApproveCommand {

    private final INotificationService iNotificationService;
    private final OutboxEventRepository outboxEventRepository;
    protected final JsonMapper jsonMapper;

    abstract BookingActionType getActionType();

    @Transactional
    public void execute(
            Booking booking,
            ApproveRequest request,
            User approver
    ) {
        // tạo Notification
        StringBuilder message = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT_NO_TZ);
        String startTimeStr = booking.getStartTime().format(formatter);
        String endTimeStr = booking.getEndTime().format(formatter);
        String timeRange = "từ " + startTimeStr + " đến " + endTimeStr;
        String roomName = booking.getRoom().getRoomName();
        User bookedBy = booking.getBookedBy();
        switch (request.actionType()) {
            case CREATED -> message.append("Lịch họp mới \"").append(booking.getTitle()).append("\"")
                    .append(" diễn ra ").append(timeRange)
                    .append(" tại ").append(roomName).append(" đã được phê duyệt");
            case UPDATED -> message.append("Thay đổi về thông tin lịch họp \"").append(booking.getTitle()).append("\"")
                    .append(" diễn ra ").append(timeRange)
                    .append(" tại ").append(roomName).append(" đã được chấp thuận");
            case ADD_EQUIPMENT ->
                    message.append("Yêu cầu cấp thêm thiết bị cho lịch họp \"").append(booking.getTitle()).append("\"")
                            .append(" diễn ra ").append(timeRange)
                            .append(" tại ").append(roomName).append(" đã được phê duyệt");
            case UPDATE_EQUIP_QUANTITY ->
                    message.append("Yêu cầu thay đổi số lượng thiết bị của lịch họp \"").append(booking.getTitle()).append("\"")
                            .append(" diễn ra ").append(timeRange).append(" đã được phê duyệt thành công");
        }
        Notification notification = iNotificationService.save(StringCommon.TITLE_NOTIFICATION, message.toString(), bookedBy);

        // tạo outbox event để gửi mail
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
