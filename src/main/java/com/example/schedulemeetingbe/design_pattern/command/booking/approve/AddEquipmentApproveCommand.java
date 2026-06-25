package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AddEquipmentApproveCommand extends BookingApproveCommand {

    public AddEquipmentApproveCommand(
            INotificationService iNotificationService,
            OutboxEventRepository outboxEventRepository,
            JsonMapper jsonMapper
    ) {
        super(iNotificationService, outboxEventRepository, jsonMapper);
    }

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.ADD_EQUIPMENT;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        super.execute(booking, request, approver);
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.now());
    }
}
