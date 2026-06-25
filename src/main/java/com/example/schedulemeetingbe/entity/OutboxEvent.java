package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.constant.enums.OutboxStatus;
import com.example.schedulemeetingbe.entity.converter.Jackson3JsonNodeConverter;
import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "event_id",
            nullable = false,
            updatable = false)
    private UUID id;

    @Column(name = "event_type",
            nullable = false,
            length = 100)
    private String eventType;

    @Convert(converter = Jackson3JsonNodeConverter.class) //jackson 3 mới chưa ổn định, phải tự viết converter
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload",
            nullable = false,
            columnDefinition = "jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false,
            length = 20)
    private OutboxStatus status;

    @Column(name = "retry_count",
            nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtils.now();
    }
}