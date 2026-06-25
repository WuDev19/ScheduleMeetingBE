package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BookingAttendee;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.composite_key.BookingAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingAttendeeRepository extends JpaRepository<BookingAttendee, BookingAttendeeId> {

    @Query("""
                SELECT ba.user
                FROM BookingAttendee ba
                WHERE ba.booking.bookingId = :bookingId
            """)
    List<User> getAttendeeOfBooking(@Param("bookingId") Long bookingId);
}
