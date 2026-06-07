package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Page<Room> findByIsActiveIsTrue(Pageable pageable);

    Page<Room> findByRoomNameContainingIgnoreCase(String keyword, Pageable pageable);
}
