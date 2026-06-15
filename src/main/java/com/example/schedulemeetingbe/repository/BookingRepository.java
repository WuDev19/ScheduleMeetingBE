package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = """
            SELECT reason FROM (
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
             ) AS overlap_check
            """, nativeQuery = true)
    List<String> checkOverlap(
            @Param("roomId") Long roomId,
            @Param("ranges") String[] ranges
    );

}
