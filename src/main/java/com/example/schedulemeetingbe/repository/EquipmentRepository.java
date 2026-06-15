package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findByEquipmentNameContainingIgnoreCase(String equipmentName, Pageable pageable);

    List<Equipment> findByEquipmentIdIn(List<Long> equipmentIds);

    @Query(value = """
            SELECT be.equipment_id AS equipmentId,
                   e.equiment_name AS equipmentName,
                   (e.total_quantity - CAST(SUM(be.quantity) AS INT)) AS remainingQuantity
            FROM equipment e
            JOIN booking_equipment be
            ON e.equipment_id = be.equipment_id
            WHERE be.equipment_id IN (:eqIds)
            GROUP BY be.equipment_id, e.equipment_name, e.total_quantity
            """,
            nativeQuery = true)
    List<EquipmentAndQuantityResponse> findEquipmentAndRemainingQuantity(@Param("eqIds") List<Long> eqIds);

}
