package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.dto.response.equipment.EquipmentAndQuantityResponse;
import com.example.schedulemeetingbe.entity.Equipment;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findByEquipmentNameContainingIgnoreCase(String equipmentName, Pageable pageable);

    List<Equipment> findByEquipmentIdIn(List<Long> equipmentIds);

    //thêm query trả về entity để áp dụng được lock, trả về projection hay dto thì ko lock đc
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
                SELECT e
                FROM Equipment e
                WHERE e.equipmentId IN :ids
                ORDER BY e.equipmentId
            """)
    List<Equipment> lockEquipments(@Param("ids") List<Long> ids);

    /*
     * khi thêm mới thì sẽ check xem số lượng equipment còn khả dụng ko
     * dựa vào công thức: available = total - using - remaining (for rollback)
     */
    @Query(value = """
            SELECT e.equipment_id AS equipmentId,
                   e.equipment_name AS equipmentName,
                    CAST (
                        (e.total_quantity
                        - COALESCE(be.used_quantity, 0)
                        - COALESCE(ber.reserved_quantity, 0)
                        ) AS INTEGER
                    ) AS remainingQuantity
            FROM equipment e
            LEFT JOIN (
                SELECT equipment_id,
                        SUM(quantity) AS used_quantity
                FROM booking_equipment
                GROUP BY equipment_id
            ) be
            ON be.equipment_id = e.equipment_id
            LEFT JOIN (
                SELECT be2.equipment_id,
                        SUM(ber.reservation_quantity) AS reserved_quantity
                FROM booking_equipment_reservation ber
                JOIN booking_equipment be2
                ON be2.booking_equipment_id = ber.booking_equipment_id
                WHERE ber.status = 'AWAIT_APPROVE'
                GROUP BY be2.equipment_id
            ) ber
            ON ber.equipment_id = e.equipment_id
            WHERE e.equipment_id IN (:eqIds)
            """,
            nativeQuery = true)
    List<EquipmentAndQuantityResponse> findEquipmentAndRemainingQuantity(@Param("eqIds") List<Long> eqIds);

    @Query(value = """
            SELECT
                e.equipment_id AS equipmentId,
                e.equipment_name AS equipmentName,
                CAST(
                    e.total_quantity
                    - COALESCE(
                        (
                            SELECT SUM(be.quantity)
                            FROM booking_equipment be
                            WHERE be.equipment_id = e.equipment_id
                        ),
                        0
                    )
                    - COALESCE(
                        (
                            SELECT SUM(ber.reservation_quantity)
                            FROM booking_equipment_reservation ber
                            JOIN booking_equipment be2
                                ON be2.booking_equipment_id = ber.booking_equipment_id
                            WHERE be2.equipment_id = e.equipment_id
                              AND ber.status = 'AWAIT_APPROVE'
                        ),
                        0
                    )
                    AS INTEGER
                ) AS remainingQuantity
            FROM equipment e
            WHERE e.equipment_id = :equipmentId
            """,
            nativeQuery = true)
    EquipmentAndQuantityResponse findEquipmentAndRemainingQuantity(@Param("equipmentId") Long equipmentId);

    @Query("""
            SELECT e
            FROM Equipment e
            WHERE e.equipmentId = :equipmentId
            """)
    Optional<Equipment> findEquipmentWithLock(@Param("equipmentId") Long equipmentId);

}
