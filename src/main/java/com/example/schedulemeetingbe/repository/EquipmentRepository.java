package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Equipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Page<Equipment> findByEquipmentNameContainingIgnoreCase(String equipmentName, Pageable pageable);
}
