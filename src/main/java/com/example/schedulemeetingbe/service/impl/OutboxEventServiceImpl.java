package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.repository.OutboxEventRepository;
import com.example.schedulemeetingbe.service.base.IOutboxEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OutboxEventServiceImpl implements IOutboxEventService {

    private final OutboxEventRepository outboxEventRepository;

    @Transactional
    @Override
    public void updateStatusSuccess(UUID eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setStatus(OutboxStatus.SUCCESS);
            event.setProcessedAt(ZonedDateTime.now(ZoneOffset.UTC));
        });
    }

    @Transactional
    @Override
    public void updateStatusDead(UUID eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setStatus(
                    event.getRetryCount() >= 5
                            ? OutboxStatus.DEAD
                            : OutboxStatus.FAILED
            );
        });
    }
}
