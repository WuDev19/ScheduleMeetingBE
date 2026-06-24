package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.recurrence.RecurrenceUserResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.RecurringPatternProjection;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecurringPatternRepository extends JpaRepository<RecurringPattern, Long>, JpaSpecificationExecutor<RecurringPattern> {

    @Query(value = """
            SELECT  rp.recurring_id AS recurringId,
                    rp.interval_value AS interval,
                    rp.days_of_week AS daysOfWeek,
                    rp.start_date AS startDate,
                    rp.end_date AS endDate,
                    rp.status AS status,
                    u.user_id AS userCreatedId,
                    u.full_name AS userCreatedName
            FROM recurring_patterns rp
            JOIN users u
            ON u.user_id = rp.created_by
            WHERE rp.status = 'PENDING'
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM recurring_patterns rp
                    JOIN users u
                    ON u.user_id = rp.created_by
                    WHERE rp.status = 'PENDING'
                    """,
            nativeQuery = true)
    Page<RecurringPatternProjection> getRecurringPatternWaiting(Pageable pageable);

    @Query(value = """
            SELECT  rp.recurring_id AS recurringId,
                    rp.interval_value AS interval,
                    rp.days_of_week AS daysOfWeek,
                    rp.start_date AS startDate,
                    rp.end_date AS endDate,
                    rp.status AS status,
                    u.user_id AS userCreatedId,
                    u.full_name AS userCreatedName
            FROM recurring_patterns rp
            JOIN users u
            ON u.user_id = rp.created_by
            WHERE u.user_id = :userId
            AND rp.status = 'PENDING'
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM recurring_patterns rp
                    JOIN users u
                    ON u.user_id = rp.created_by
                    WHERE u.user_id = :userId
                    AND rp.status = 'PENDING'
                    """,
            nativeQuery = true)
    Page<RecurringPatternProjection> getMyRecurringPattern(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT new com.example.schedulemeetingbe.dto.response.recurrence.RecurrenceUserResponse(
                    rp.recurringId,
                    rp.createdBy.userId,
                    rp.createdBy.fullName
            )
            FROM RecurringPattern rp
            WHERE rp.recurringId IN (:recurringIds)
            """)
    List<RecurrenceUserResponse> getUserCreatedRecurrence(@Param("recurringIds") List<Long> recurringIds);
}
