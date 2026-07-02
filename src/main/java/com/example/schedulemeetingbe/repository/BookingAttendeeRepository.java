package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndEmailAttendeeResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndUserResponse;
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

    @Query("""
                SELECT new com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndUserResponse(
                   ba.booking.bookingId,
                   ba.user
                )
                FROM BookingAttendee ba
                WHERE ba.booking.bookingId IN :bookingIds
            """)
    List<BookingAndUserResponse> getAttendeeOfBooking(@Param("bookingIds") List<Long> bookingIds);

    @Query("""
                SELECT new com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingAndEmailAttendeeResponse(
                   ba.booking.bookingId,
                   ba.user.email
                )
                FROM BookingAttendee ba
                WHERE ba.booking.bookingId IN :bookingIds
            """)
    List<BookingAndEmailAttendeeResponse> getEmailAttendOfBooking(@Param("bookingIds") List<Long> bookingIds);

    @Query("""
        SELECT ba.user.email
        FROM BookingAttendee ba
        WHERE ba.booking.bookingId = :bookingId
        """)
    List<String> getEmailAttendee(@Param("bookingId") Long bookingId);

}
