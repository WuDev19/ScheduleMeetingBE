package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.BookingHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingHistoryRepository extends JpaRepository<BookingHistory, Long> {
}
