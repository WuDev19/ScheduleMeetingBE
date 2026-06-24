package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.payload.ApproveRejectRecurrencePayload;
import com.example.schedulemeetingbe.entity.payload.BookingCancelledByMaintenancePayload;
import com.example.schedulemeetingbe.entity.payload.ReceiverEmailPayload;

public interface IEmailService {
    void sendEmailActiveAccount(String email, String token);

    void sendEmailResetPassword(String email);

    void sendEmailUsernamePassword(String email, String username, String password);

    void sendEmailUpdateEmail(String newEmail, String token);

    void sendEmailCancelledBookingByMaintain(BookingCancelledByMaintenancePayload payload);

    void sendBulkEmailBookingContent(ReceiverEmailPayload payload);

    void sendEmailApproveReject(ApproveRejectRecurrencePayload payload);
}
