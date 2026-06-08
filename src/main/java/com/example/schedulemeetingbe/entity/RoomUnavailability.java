package com.example.schedulemeetingbe.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Entity
@Table(name = "room_unavailability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class RoomUnavailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "unavailable_id")
    private Long unavailableId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(length = 255)
    private String reason;

    @Column(name = "start_time", nullable = false)
    private ZonedDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private ZonedDateTime endTime;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now(ZoneOffset.UTC);
    }
}
