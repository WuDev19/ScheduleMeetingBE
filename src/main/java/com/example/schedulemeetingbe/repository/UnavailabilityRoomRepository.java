package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.RoomUnavailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UnavailabilityRoomRepository extends JpaRepository<RoomUnavailability, Long> {

    Page<RoomUnavailability> findByReasonContainingIgnoreCase(String reason, Pageable pageable);
}
