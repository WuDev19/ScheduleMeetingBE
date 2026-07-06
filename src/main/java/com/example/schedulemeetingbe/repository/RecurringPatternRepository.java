package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.recurrence.RecurrenceUserResponse;
import com.example.schedulemeetingbe.entity.RecurringPattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecurringPatternRepository extends JpaRepository<RecurringPattern, Long>, JpaSpecificationExecutor<RecurringPattern> {

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
