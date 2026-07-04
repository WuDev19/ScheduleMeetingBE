package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.payload.*;

public interface IEmailService {
    void sendEmailActiveAccount(String email, String token);

    void sendEmailResetPassword(String email);

    void sendEmailUsernamePassword(String email, String username, String password);

    void sendEmailUpdateEmail(String newEmail, String token);

    void sendEmailCancelledBookingByMaintain(BookingCancelledByMaintenancePayload payload);

    void sendBulkEmailBookingContent(ReceiverEmailPayload payload);

    void sendEmailApproveReject(ApproveRejectRecurrencePayload payload);

    void sendEmailRemindingBooking(RemindingBookingPayload payload);

    void sendEmailApproveUpdate(UpdateApprovePayload payload);

    void sendEmailCancelBooking(CancelBookingPayload payload);

    void sendEmailCancelBookingToAttendee(SimpleCancelBookingPayload payload);
}
