package com.example.schedulemeetingbe.entity;

import jakarta.persistence.*;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
@Entity
@Table(
        name = "booking_equipment",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_booking_equipment",
                columnNames = {"booking_id", "equipment_id"}
        )
)
public class BookingEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_equipment_id")
    private Long bookingEquipmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

}