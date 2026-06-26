package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.constant.enums.BookingActionType;
import com.example.schedulemeetingbe.constant.enums.BookingStatus;
import com.example.schedulemeetingbe.dto.response.booking.BookingHistoryResponse;
import com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.booking.BookingRemainingResponse;
import com.example.schedulemeetingbe.dto.response.booking.BookingSummaryProjection;
import com.example.schedulemeetingbe.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @EntityGraph(attributePaths = {"room", "room.building", "bookedBy"})
    Page<Booking> findAllWithRoomAndBookedBy(org.springframework.data.jpa.domain.Specification<Booking> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"room", "room.building", "bookedBy"})
    List<Booking> findAllWithRoomAndBookedBy(org.springframework.data.jpa.domain.Specification<Booking> spec);

    @EntityGraph(attributePaths = {"room", "room.building", "bookedBy"})
    @Query(""
            + "select distinct b from Booking b "
            + "left join b.attendees a "
            + "where (b.bookedBy.userId = :userId or a.userId = :userId) "
            + "and b.status in :statuses "
            + "and b.deletedAt is null"
            + ""
    )
    List<Booking> findAllBookingsForRegisterExport(@Param("userId") Long userId, @Param("statuses") List<BookingStatus> statuses);

    @EntityGraph(attributePaths = {"room", "room.building", "bookedBy"})
    @Query("select b from Booking b where b.deletedAt is null")
    List<Booking> findAllBookingsForApproverExport();

    //check lúc cập nhật
    @Query(value = """
                SELECT CONCAT('Phòng họp không khả dụng vì ', ru.reason) AS reason
                FROM room_unavailability ru
                WHERE ru.room_id = :roomId
                AND tstzrange(ru.start_time, ru.end_time)
                                    && ANY((:ranges)::tstzrange[])
            
                UNION ALL
            
                SELECT CONCAT('Bị trùng với ', b.title) AS reason
                FROM bookings b
                WHERE b.room_id = :roomId
                AND b.status NOT IN ('CANCELLED', 'REJECTED')
                AND b.deleted_at IS NULL
                AND b.booking_id <> :currentBookingId
                AND tstzrange(b.start_time, b.end_time)
                                    && ANY((:ranges)::tstzrange[])
            
                UNION ALL 
            
                SELECT concat('Phòng và thời gian không khả dụng vì chưa được duyệt') AS reason
                FROM booking_reservation br
                WHERE br.status = 'AWAIT_APPROVE'
                AND br.booking_id <> :currentBookingId
                AND tstzrange(br.old_start_time, br.old_end_time)
                            && ANY((:ranges)::tstzrange[])
                LIMIT 1
            
            """,
            nativeQuery = true)
    List<String> checkOverlap(
            @Param("currentBookingId") Long currentBookingId,
            @Param("roomId") Long roomId,
            @Param("ranges") String[] ranges //truyền mảng để có thể sử dụng hàm này cho cả booking 1 lần và booking theo chu kì
    );

    //check lúc tạo mới thì chưa có booking_id
    @Query(value = """
                SELECT CONCAT('Phòng họp không khả dụng vì ', ru.reason) AS reason
                FROM room_unavailability ru
                WHERE ru.room_id = :roomId
                AND tstzrange(ru.start_time, ru.end_time)
                                    && ANY((:ranges)::tstzrange[])
            
                UNION ALL
            
                SELECT CONCAT('Bị trùng với ', b.title) AS reason
                FROM bookings b
                WHERE b.room_id = :roomId
                AND b.status NOT IN ('CANCELLED', 'REJECTED')
                AND b.deleted_at IS NULL
                AND tstzrange(b.start_time, b.end_time)
                                    && ANY((:ranges)::tstzrange[])
            
                UNION ALL 
            
                SELECT concat('Phòng và thời gian không khả dụng vì chưa được duyệt') AS reason
                FROM booking_reservation br
                WHERE br.status = 'AWAIT_APPROVE'
                AND tstzrange(br.old_start_time, br.old_end_time)
                            && ANY((:ranges)::tstzrange[])
                LIMIT 1
            
            """,
            nativeQuery = true)
    List<String> checkOverlap(
            @Param("roomId") Long roomId,
            @Param("ranges") String[] ranges //truyền mảng để có thể sử dụng hàm này cho cả booking 1 lần và booking theo chu kì
    );

    @Query(value = """
            SELECT 
                b.booking_id AS bookingId,
                bh.history_id AS historyId,
                b.title AS title,
                u.full_name AS userBooked,
                u.phone AS phone,
                r.room_name AS roomName,
                b.status AS status,
                bh.action_type AS actionType,
                b.start_time AS startTime,
                b.end_time AS endTime
            FROM bookings b
            JOIN rooms r ON r.room_id = b.room_id 
            JOIN users u ON u.user_id = b.booked_by 
            JOIN booking_history bh ON bh.booking_id = b.booking_id
            WHERE b.status = 'PENDING'
            AND bh.is_revoked = false
            AND bh.action_type IN ('UPDATED', 'ADD_EQUIPMENT', 'UPDATE_EQUIP_QUANTITY', 'CREATED')   
            AND b.title IS NOT NULL
            ORDER BY bh.created_at DESC 
            """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM bookings b
                    JOIN rooms r ON r.room_id = b.room_id 
                    JOIN users u ON u.user_id = b.booked_by 
                    JOIN booking_history bh ON bh.booking_id = b.booking_id
                    WHERE b.status = 'PENDING'
                    AND bh.is_revoked = false
                    AND bh.action_type IN ('UPDATED', 'ADD_EQUIPMENT', 'UPDATE_EQUIP_QUANTITY', 'CREATED') 
                    AND b.title IS NOT NULL
                    """,
            nativeQuery = true)
    Page<BookingSummaryProjection> getBookingWaitingApprove(Pageable pageable);

    @Query(value = """
            SELECT 
                b.booking_id AS bookingId,
                b.title AS title,
                b.description AS discription,
                r.room_name AS roomName,
                bd.address AS roomAddress,
                r.floor_number AS floorNumber,
                u.full_name AS fullName,
                u.email AS email,
                u.phone AS phone,
                b.start_time AS startTime,
                b.end_time AS endTime,
                b.attendee_count AS attendee,
                bh.history_id AS bookingHistoryId,
                uc.full_name AS userChanged,
                bh.action_type AS actionType,
                bh.old_data AS oldData,
                bh.new_data AS newData
            FROM booking_history bh
            JOIN bookings b
            ON b.booking_id = bh.booking_id
            JOIN rooms r 
            ON r.room_id = b.room_id
            JOIN buildings bd
            ON bd.building_id = r.building_id
            JOIN users u
            ON u.user_id = b.booked_by
            JOIN users uc
            ON uc.user_id = bh.changed_by
            WHERE bh.history_id = :historyId
            """,
            nativeQuery = true)
    BookingHistoryResponse getDetailBookingWaitingToApprove(@Param("historyId") Long historyId);

    @Modifying
    @Query(value = """
            UPDATE BookingHistory bh
            SET bh.isRevoked = true
            WHERE bh.booking.bookingId = :bookingId
            AND bh.actionType = :actionType
            """)
    void revokeAllOldChangeHistory(
            @Param("bookingId") Long bookingId,
            @Param("actionType") BookingActionType actionType
    );

    @Modifying
    @Query(value = """
            UPDATE bookings 
            SET status = 'CANCEL',
                cancellation_reason = :reason, 
                cancelled_at = :cancelledAt
            WHERE recurring_id = :recurringId
            """,
            nativeQuery = true)
    void cancelBookingByRecurringPattern(@Param("recurringId") Long recurringId,
                                         @Param("reason") String reason,
                                         @Param("cancelledAt") OffsetDateTime cancelledAt);

    @Modifying
    @Query(value = """
            UPDATE Booking 
            SET status = :status,
                approvedBy.userId = :approvedBy, 
                approvedAt = :approvedAt
            WHERE recurringPattern.recurringId = :recurringId
            """)
    void approveOrRejectBookingByRecurringPattern(
            @Param("recurringId") Long recurringId,
            @Param("status") BookingStatus status,
            @Param("approvedBy") Long approvedBy,
            @Param("approvedAt") OffsetDateTime approvedAt
    );

    @Query("""
            SELECT new  com.example.schedulemeetingbe.dto.response.booking.BookingRecurrenceResponse(
                b.bookingId,
                b.recurringPattern.recurringId,
                b.startTime,
                b.endTime,
                b.status
                )
            FROM Booking b
            WHERE b.recurringPattern.recurringId IN (:recurringIds)
            """)
    List<BookingRecurrenceResponse> getBookingByRecurrence(@Param("recurringIds") List<Long> recurringIds);

    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.status = 'APPROVED'
            """)
    List<Booking> findByStatusApproved();

    @Query("""
            SELECT new com.example.schedulemeetingbe.dto.response.booking.BookingRemainingResponse(
                b.title,
                b.room.roomName
            )
            FROM Booking b
            WHERE b.bookingId = :bookingId
            """)
    Optional<BookingRemainingResponse> getBookingRemain(@Param("bookingId") Long bookingId);
}
