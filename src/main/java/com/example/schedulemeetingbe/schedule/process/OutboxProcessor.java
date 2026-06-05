package com.example.schedulemeetingbe.schedule.process;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.payload.UserChangeEmailPayload;
import com.example.schedulemeetingbe.entity.payload.UserCreatePayload;
import com.example.schedulemeetingbe.entity.payload.UserRegisteredPayload;
import com.example.schedulemeetingbe.entity.payload.UserResetPasswordPayload;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final JsonMapper jsonMapper;
    private final IEmailService iEmailService;
    private final OutboxEventRepository outboxEventRepository;

    @Async("outboxExecutor")
    @Transactional
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
            }
            event.setStatus(OutboxStatus.SUCCESS);
            event.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        } catch (Exception ex) {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setStatus(
                    event.getRetryCount() >= 5
                            ? OutboxStatus.DEAD
                            : OutboxStatus.FAILED
            );
        }
    }
}
