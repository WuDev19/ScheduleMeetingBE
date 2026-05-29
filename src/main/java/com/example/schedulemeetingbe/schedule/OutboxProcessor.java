package com.example.schedulemeetingbe.schedule;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.payload.UserRegisteredPayload;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@EnableScheduling
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final IEmailService iEmailService;

    @Scheduled(fixedDelay = 10000)
    @Transactional
    public void process() {
        List<OutboxEvent> events = outboxRepository.findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        events.forEach(event -> {
            try {
                event.setStatus(OutboxStatus.PROCESSING);
                UserRegisteredPayload payload = objectMapper.treeToValue(event.getPayload(), UserRegisteredPayload.class);
                iEmailService.sendEmailActiveAccount(payload.email(), payload.token());
                event.setStatus(OutboxStatus.SUCCESS);
                event.setProcessedAt(ZonedDateTime.now());
            } catch (Exception e) {
                event.setRetryCount(event.getRetryCount() + 1);
                event.setStatus(OutboxStatus.FAILED);
            }
        });
    }
}
