package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {

    Page<Room> findByIsActiveIsTrue(Pageable pageable);

    @Query("""
            SELECT DISTINCT re.room
            FROM RoomEquipment re
            WHERE (LOWER(re.room.roomName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(re.equipment.equipmentName) LIKE LOWER(CONCAT('%', :keyword, '%')))
                                AND re.room.deletedAt IS NULL
            """)
    Page<Room> searchRoom(@Param("keyword") String keyword, Pageable pageable);
}
