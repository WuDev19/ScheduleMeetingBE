package com.example.schedulemeetingbe.dto.request.department;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
        @Size(max = 100, message = "Tên phòng ban không vượt quá 100 ký tự")
        String departmentName,

        @Size(max = 20, message = "Mã phòng ban không vượt quá 20 ký tự")
        String departmentCode,

        String description
) {
}
