package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Table(
        name = "rooms",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"room_name", "building_id"})},
        indexes = {
                @Index(name = "idx_rooms_building", columnList = "building_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    private Building building;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @Column(name = "deleted_at")
    private ZonedDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtils.ZONE_DATE_TIME;
        updatedAt = TimeUtils.ZONE_DATE_TIME;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimeUtils.ZONE_DATE_TIME;
    }
}
