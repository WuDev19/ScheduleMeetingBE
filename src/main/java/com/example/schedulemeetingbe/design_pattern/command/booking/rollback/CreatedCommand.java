package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class CreatedCommand extends BookingRollbackCommand {

    public CreatedCommand(
            INotificationService iNotificationService,
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper
    ) {
        super(iNotificationService, outboxEventRepository, jsonMapper);
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.CREATED;
    }

    @Override
    public void execute(Booking booking, RollBackRequest request, User approver) {
        super.execute(booking, request, approver);
        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
