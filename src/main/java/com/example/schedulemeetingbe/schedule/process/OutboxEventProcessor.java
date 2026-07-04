package com.example.schedulemeetingbe.schedule.process;

import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.payload.*;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.ICloudinaryService;
import com.example.schedulemeetingbe.service.base.IEmailService;
import com.example.schedulemeetingbe.service.base.IOutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventProcessor {

    private final JsonMapper jsonMapper;
    private final IEmailService iEmailService;
    private final ICloudinaryService iCloudinaryService;
    private final OutboxEventRepository outboxEventRepository;
    private final IOutboxEventService iOutboxEventService;

    //vẫn chưa xử lý đc case đang PROCESSING thì server sập => kẹt ở PROCESSING
    //thêm một cái scheduler nữa check cái processing nếu ở trạng thái processing lâu thì đưa về failed
    //tối ưu sử dụng command pattern sau
    @Async("outboxExecutor")
    public void processEvent(UUID eventId) {
        Optional<OutboxEvent> eventOptional = outboxEventRepository.findById(eventId);
        if (eventOptional.isEmpty()) return;
        OutboxEvent event = eventOptional.get();
        try {
            switch (event.getEventType()) {
                case "USER_REGISTER", "RESEND_EMAIL" -> {
                    UserRegisteredPayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    UserRegisteredPayload.class
                            );

                    iEmailService.sendEmailActiveAccount(
                            payload.email(),
                            payload.token()
                    );
                }
                case "CREATE_USER" -> {
                    UserCreatePayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    UserCreatePayload.class
                            );

                    iEmailService.sendEmailUsernamePassword(
                            payload.email(),
                            payload.username(),
                            payload.password()
                    );
                }
                case "RESET_PASSWORD" -> {
                    UserResetPasswordPayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    UserResetPasswordPayload.class
                            );
                    iEmailService.sendEmailResetPassword(payload.email());
                }
                case "UPDATE_EMAIL" -> {
                    UserChangeEmailPayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    UserChangeEmailPayload.class
                            );
                    iEmailService.sendEmailUpdateEmail(payload.newEmail(), payload.token());
                }
                case "DELETE_AVATAR" -> {
                    UserDeleteAvatarPayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    UserDeleteAvatarPayload.class
                            );
                    iCloudinaryService.delete(payload.publicId());
                }
                case "BOOKING_CANCELLED_BY_MAINTENANCE" -> {
                    BookingCancelledByMaintenancePayload payload =
                            jsonMapper.treeToValue(
                                    event.getPayload(),
                                    BookingCancelledByMaintenancePayload.class
                            );
                    iEmailService.sendEmailCancelledBookingByMaintain(payload);
                }
                case "SEND_EMAIL_CONFIRM_PARTICIPATE" -> {
                    ReceiverEmailPayload payload = jsonMapper.treeToValue(
                            event.getPayload(),
                            ReceiverEmailPayload.class
                    );
                    iEmailService.sendBulkEmailBookingContent(payload);
                }
                case "SEND_EMAIL_APPROVE_REJECT" -> {
                    ApproveRejectRecurrencePayload payload = jsonMapper.treeToValue(
                            event.getPayload(),
                            ApproveRejectRecurrencePayload.class);
                    iEmailService.sendEmailApproveReject(payload);
                }
                case "SEND_EMAIL_APPROVE_UPDATE" -> {
                    UpdateApprovePayload payload = jsonMapper.treeToValue(
                            event.getPayload(),
                            UpdateApprovePayload.class
                    );
                    iEmailService.sendEmailApproveUpdate(payload);
                }
                case "CANCEL_BOOKING_BY_REGISTER" -> {
                    CancelBookingPayload payload = jsonMapper.treeToValue(
                            event.getPayload(),
                            CancelBookingPayload.class
                    );
                    iEmailService.sendEmailCancelBooking(payload);
                }
                case "CANCEL_BOOKING_BY_MAINTENANCE_TO_ATTENDEE" -> {
                    SimpleCancelBookingPayload payload = jsonMapper.treeToValue(
                            event.getPayload(),
                            SimpleCancelBookingPayload.class
                    );
                    iEmailService.sendEmailCancelBookingToAttendee(payload);
                }
            }
            iOutboxEventService.updateStatusSuccess(event.getId());
        } catch (Exception ex) {
            iOutboxEventService.updateStatusDead(event.getId());
        }
    }

}
