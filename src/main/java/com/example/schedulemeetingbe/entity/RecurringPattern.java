package com.example.schedulemeetingbe.entity;

import com.example.schedulemeeting.model.enumeration.type.BookingStatus;
import com.example.schedulemeeting.model.enumeration.type.RecurrenceType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

@Entity
@Table(name = "recurring_patterns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PUBLIC)
public class RecurringPattern {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recurring_id")
    private Long recurringId;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_type", nullable = false)
    private RecurrenceType recurrenceType;

    @Column(name = "interval_value", nullable = false)
    @Builder.Default
    private Integer intervalValue = 1;

    @Column(name = "days_of_week", length = 50)
    private String daysOfWeek;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.pending;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
    }
}
