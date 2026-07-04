package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCode(String departmentCode);

    boolean existsByDepartmentName(String departmentName);

    List<Department> findByDepartmentCodeIn(List<String> departmentCodes);

    Page<Department> findByDeletedAtIsNull(Pageable pageable);

    @Query("""
            SELECT d
            FROM Department d
            WHERE d.deletedAt IS NULL
            AND (LOWER(d.departmentName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(d.departmentCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Department> searchDepartments(@Param("keyword") String keyword, Pageable pageable);
}
