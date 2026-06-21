package com.example.schedulemeetingbe.command.booking.approve;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.utils.TimeUtils;
import org.springframework.stereotype.Component;

@Component
public class CreatedApproveCommand implements BookingApproveCommand{
    @Override
    public BookingActionType getActionType() {
        return BookingActionType.CREATED;
    }

    @Override
    public void execute(Booking booking, ApproveRequest request, User approver) {
        booking.setStatus(BookingStatus.APPROVED);
        booking.setApprovedBy(approver);
        booking.setApprovedAt(TimeUtils.ZONE_DATE_TIME);
    }
}
