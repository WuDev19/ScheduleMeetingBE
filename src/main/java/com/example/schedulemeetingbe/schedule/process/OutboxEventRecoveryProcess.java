package com.example.schedulemeetingbe.schedule.process;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class OutboxEventRecoveryProcess {

    private final OutboxEventRepository outboxEventRepository;

    public void processEvent(UUID eventId) {
        Optional<OutboxEvent> eventOptional = outboxEventRepository.findById(eventId);
        if (eventOptional.isEmpty()) return;
        OutboxEvent event = eventOptional.get();
        if (ChronoUnit.MINUTES.between(event.getProcessedAt(), TimeUtils.ZONE_DATE_TIME) > 10) {
            event.setStatus(OutboxStatus.FAILED);
            outboxEventRepository.save(event);
        }
    }

}
