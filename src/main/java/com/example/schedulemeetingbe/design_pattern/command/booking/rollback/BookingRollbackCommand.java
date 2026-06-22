package com.example.schedulemeetingbe.design_pattern.command.booking.rollback;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.dto.request.booking.RollBackRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;

public interface BookingRollbackCommand {
    BookingActionType getActionType();

    void execute(
            Booking booking,
            RollBackRequest request,
            User approver
    );
}
