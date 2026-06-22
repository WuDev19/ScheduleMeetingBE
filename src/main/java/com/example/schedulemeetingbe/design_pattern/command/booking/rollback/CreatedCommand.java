package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;

@Component
public class CreatedCommand implements BookingRollbackCommand {

    @Override
    public BookingActionType getActionType() {
        return BookingActionType.CREATED;
    }

    @Override
    public void execute(Booking booking, RollBackRequest request, User approver) {
        booking.setStatus(BookingStatus.REJECTED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
