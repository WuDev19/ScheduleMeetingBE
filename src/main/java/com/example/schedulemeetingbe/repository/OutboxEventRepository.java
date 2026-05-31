package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.constant.enums.EVENT_TYPE;
import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {
    List<OutboxEvent> findTop10ByStatusInAndEventTypeInOrderByCreatedAtAsc(List<OutboxStatus> status, List<String> eventType);
}
