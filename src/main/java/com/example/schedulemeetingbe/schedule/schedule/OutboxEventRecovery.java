package com.example.schedulemeetingbe.schedule.schedule;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.schedule.process.OutboxEventRecoveryProcess;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxEventRecovery {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxEventRecoveryProcess outboxEventRecoveryProcess;

    @Scheduled(fixedDelay = 15000)
    public void process() {
        List<OutboxEvent> events = outboxEventRepository.findTop20ByStatusOrderByCreatedAtAsc(
                OutboxStatus.PROCESSING
        );
        events.forEach(event -> outboxEventRecoveryProcess.processEvent(event.getId()));
    }
}
