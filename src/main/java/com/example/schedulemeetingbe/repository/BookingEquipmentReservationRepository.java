package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BookingEquipmentReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingEquipmentReservationRepository extends JpaRepository<BookingEquipmentReservation, Long> {

    Optional<BookingEquipmentReservation> findByBookingEquipment_BookingEquipmentId(Long bookingEquipmentId);
}
