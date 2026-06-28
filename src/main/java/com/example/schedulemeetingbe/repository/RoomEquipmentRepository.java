package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.equipment.RoomEquipmentResponse;
import com.example.schedulemeetingbe.entity.RoomEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomEquipmentRepository extends JpaRepository<RoomEquipment, Long> {

    @Query("""
            SELECT new com.example.schedulemeetingbe.dto.response.equipment.RoomEquipmentResponse(
                        re.room.roomId,
                        e.equipmentId,
                        e.equipmentName,
                        e.description,
                        re.quantity
            )
            FROM RoomEquipment re
            JOIN re.equipment e
            WHERE re.room.roomId IN :roomIds
            """)
    List<RoomEquipmentResponse> findEquipmentByRoomId(@Param("roomIds") List<Long> roomIds);

    List<RoomEquipment> findRoomEquipmentByRoom_RoomId(Long roomId);
}
