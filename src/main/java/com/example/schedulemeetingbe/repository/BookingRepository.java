package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
}
