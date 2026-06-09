package com.example.schedulemeetingbe.service.base;

import java.util.UUID;

public interface IOutboxEventService {
    void updateStatusSuccess(UUID eventId);
    void updateStatusDead(UUID eventId);
}
