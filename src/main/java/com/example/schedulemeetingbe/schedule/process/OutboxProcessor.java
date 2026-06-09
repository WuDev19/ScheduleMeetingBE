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
public class OutboxProcessor {

    private final JsonMapper jsonMapper;
    private final IEmailService iEmailService;
    private final ICloudinaryService iCloudinaryService;
    private final OutboxEventRepository outboxEventRepository;
    private final IOutboxEventService iOutboxEventService;

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
            }
            iOutboxEventService.updateStatusSuccess(event.getId());
        } catch (Exception ex) {
            iOutboxEventService.updateStatusDead(event.getId());
        }
    }
}
