package com.example.schedulemeetingbe.entity;

import com.example.schedulemeetingbe.constant.enums.ReservationStatus;
import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;

//bảng này thiết kế để lưu lại các slot thiết bị khi mình cập nhật SL thiết bị từ SL lớn -> SL nhỏ mà Approver chưa duyệt để giữ chỗ
@Entity
@Table(name = "booking_equipment_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class BookingEquipmentReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_reservation_id")
    private Long equipmentReservationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "booking_equipment_id",
            referencedColumnName = "booking_equipment_id",
            unique = true
    )
    private BookingEquipment bookingEquipment;

    @Column(name = "reservation_quantity")
    private Integer reservationQuantity;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private ReservationStatus status = ReservationStatus.AWAIT_APPROVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = TimeUtils.now();
        updatedAt = TimeUtils.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = TimeUtils.now();
    }
}