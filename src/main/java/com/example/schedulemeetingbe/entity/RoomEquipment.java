package com.example.schedulemeetingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "room_equipment", uniqueConstraints = {@UniqueConstraint(columnNames = {"room_id", "equipment_id"})})
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class RoomEquipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_equipment_id")
    private Long roomEquipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 1;
}
