package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BuildingRepository extends JpaRepository<Building, Long> {

    @Query("""
            SELECT b
            FROM Building b
            WHERE LOWER(b.buildingName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(b.address) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY b.createdAt DESC
            """)
    Page<Building> search(@Param("keyword") String keyword, Pageable pageable);

}
