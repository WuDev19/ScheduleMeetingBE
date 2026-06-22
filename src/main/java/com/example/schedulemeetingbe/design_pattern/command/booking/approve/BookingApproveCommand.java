package com.example.schedulemeetingbe.design_pattern.command.booking.approve;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.dto.request.booking.ApproveRequest;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.User;

public interface BookingApproveCommand {
    BookingActionType getActionType();

    void execute(
            Booking booking,
            ApproveRequest request,
            User approver
    );
}
