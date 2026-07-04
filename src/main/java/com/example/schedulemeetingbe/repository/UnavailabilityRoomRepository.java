package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.RoomUnavailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UnavailabilityRoomRepository extends JpaRepository<RoomUnavailability, Long>, JpaSpecificationExecutor<RoomUnavailability> {

    Page<RoomUnavailability> findByReasonContainingIgnoreCaseOrderByCreatedAtDesc(String reason, Pageable pageable);

    Page<RoomUnavailability> findByReasonContainingIgnoreCaseAndIsDeletedIsFalseOrderByCreatedAtDesc(String reason, Pageable pageable);

    Page<RoomUnavailability> findByReasonContainingIgnoreCaseAndIsDeletedIsTrueOrderByCreatedAtDesc(String reason, Pageable pageable);

    Page<RoomUnavailability> findByIsDeletedIsFalseOrderByCreatedAtDesc(Pageable pageable);

    Page<RoomUnavailability> findByIsDeletedIsTrueOrderByCreatedAtDesc(Pageable pageable);
}
