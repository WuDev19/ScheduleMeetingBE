package com.example.schedulemeetingbe.mapper;

import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;
import com.example.schedulemeetingbe.entity.Department;
import com.example.schedulemeetingbe.entity.User;

public class UserMapper {
    private UserMapper() {
    }

    public static UserDetailResponse mapToUserDetailResponse(User user) {
        return new UserDetailResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                mapToDepartmentResponse(user.getDepartment())
        );
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
