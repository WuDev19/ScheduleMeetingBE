package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BookingAttendee;
import com.example.schedulemeetingbe.entity.composite_key.BookingAttendeeId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingAttendeeRepository extends JpaRepository<BookingAttendee, BookingAttendeeId> {
}
