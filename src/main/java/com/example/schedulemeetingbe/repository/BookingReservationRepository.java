package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BookingReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingReservationRepository extends JpaRepository<BookingReservation, Long> {
    Optional<BookingReservation> findBookingReservationsByBooking_BookingId(Long bookingId);
}
