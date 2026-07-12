package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

import java.time.OffsetDateTime;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Page<Room> findByIsActiveIsTrue(Pageable pageable);

    Page<Room> findByRoomNameContainingIgnoreCase(String keyword, Pageable pageable);

    @Query(value = """
            SELECT r.*
            FROM rooms r
            WHERE NOT EXISTS (
                SELECT 1
                FROM bookings b
                WHERE b.room_id = r.room_id
                  AND b.status NOT IN ('CANCELLED', 'REJECTED')
                  AND tstzrange(b.start_time, b.end_time) && tstzrange(:start, :end)
                )
            AND NOT EXISTS (
                SELECT 1
                FROM room_unavailability ru
                WHERE ru.room_id = r.room_id
                  AND tstzrange(ru.start_time, ru.end_time) && tstzrange(:start, :end)
                )
            AND NOT EXISTS (
                SELECT 1
                FROM booking_reservation br
                WHERE br.old_room_id = r.room_id
                  AND br.status = 'AWAIT_APPROVE'
                  AND tstzrange(br.old_start_time, br.old_end_time) && tstzrange(:start, :end)
                )
            """,
            countQuery = """
                    SELECT COUNT(r.id)
                    FROM rooms r
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM bookings b
                        WHERE b.room_id = r.room_id
                          AND b.status NOT IN ('CANCELLED', 'REJECTED')
                          AND tstzrange(b.start_time, b.end_time) && tstzrange(:start, :end)
                        )
                    AND NOT EXISTS (
                        SELECT 1
                        FROM room_unavailability ru
                        WHERE ru.room_id = r.room_id
                          AND tstzrange(ru.start_time, ru.end_time) && tstzrange(:start, :end)
                        )
                    AND NOT EXISTS (
                        SELECT 1
                        FROM booking_reservation br
                        WHERE br.old_room_id = r.room_id
                          AND br.status = 'AWAIT_APPROVE'
                          AND tstzrange(br.old_start_time, br.old_end_time) && tstzrange(:start, :end)
                        )
                    """,
            nativeQuery = true)
    Page<Room> findRoomNotOverlap(
            @Param("start") OffsetDateTime start,
            @Param("end") OffsetDateTime end,
            Pageable pageable
    );

}
