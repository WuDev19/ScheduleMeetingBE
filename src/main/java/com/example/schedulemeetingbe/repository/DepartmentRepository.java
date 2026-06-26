package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByDepartmentCode(String departmentCode);

    boolean existsByDepartmentCode(String departmentCode);

    boolean existsByDepartmentName(String departmentName);

    List<Department> findByDepartmentCodeIn(List<String> departmentCodes);

    Page<Department> findByDeletedAtIsNull(Pageable pageable);
}
