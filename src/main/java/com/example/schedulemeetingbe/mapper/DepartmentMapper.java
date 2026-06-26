package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.entity.Department;

public final class DepartmentMapper {

    private DepartmentMapper() {
    }

    public static DepartmentResponse mapToDepartmentResponse(Department department) {
        return new DepartmentResponse(
                department.getDepartmentId(),
                department.getDepartmentName(),
                department.getDepartmentCode(),
                department.getDescription()
        );
    }
}
