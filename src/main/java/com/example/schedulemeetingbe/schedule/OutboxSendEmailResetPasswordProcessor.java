package com.example.schedulemeetingbe.schedule;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.entity.payload.UserResetPasswordPayload;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxSendEmailResetPasswordProcessor {
    private final OutboxEventRepository outboxRepository;
    private final JsonMapper jsonMapper;
    private final IEmailService iEmailService;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void process() {
        List<OutboxEvent> events = outboxRepository.findTop10ByStatusInAndEventTypeInOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED),
                List.of(EVENT_TYPE.RESET_PASSWORD.name())
        );
        events.forEach(event -> {
            try {
                UserResetPasswordPayload payload = jsonMapper.treeToValue(event.getPayload(), UserResetPasswordPayload.class);
                iEmailService.sendEmailResetPassword(payload.email());
                event.setStatus(OutboxStatus.SUCCESS);
                event.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 5)
                    event.setStatus(OutboxStatus.DEAD);
                else
                    event.setStatus(OutboxStatus.FAILED);
            }
        });
    }
}
