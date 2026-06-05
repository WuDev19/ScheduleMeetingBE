package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
