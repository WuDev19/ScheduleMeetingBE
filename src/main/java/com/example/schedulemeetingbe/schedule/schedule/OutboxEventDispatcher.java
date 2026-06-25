package com.example.schedulemeetingbe.schedule.schedule;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.schedule.process.OutboxEventProcessor;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventDispatcher {
    private final OutboxEventProcessor outboxEventProcessor;
    private final OutboxEventRepository outboxEventRepository;

    @Scheduled(fixedDelay = 10000)
    public void process() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusInOrderByCreatedAtAsc(
                List.of(OutboxStatus.PENDING, OutboxStatus.FAILED)
        );
        events.forEach(event -> {
            event.setStatus(OutboxStatus.PROCESSING);
            event.setProcessedAt(TimeUtils.now());
        });
        outboxEventRepository.saveAll(events);
        events.forEach(event -> outboxEventProcessor.processEvent(event.getId()));
    }
}
