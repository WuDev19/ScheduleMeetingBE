package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "booking_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class BookingReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "old_room_id")
    private Room oldRoom;

    @Column(name = "old_start_time")
    private ZonedDateTime oldStartTime;

    @Column(name = "old_end_time")
    private ZonedDateTime oldEndTime;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private ReservationStatus status = ReservationStatus.AWAIT_APPROVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

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
