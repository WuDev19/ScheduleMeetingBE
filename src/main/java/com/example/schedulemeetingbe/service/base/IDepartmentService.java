package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.department.CreateDepartmentRequest;
import com.example.schedulemeetingbe.dto.request.department.UpdateDepartmentRequest;
import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface IDepartmentService {
    Map<String, Object> createDepartment(CreateDepartmentRequest request);

    DepartmentResponse getDepartment(Long id);

    PageResponse<DepartmentResponse> getDepartments(String keyword, Pageable pageable);

    Map<String, Object> updateDepartment(Long id, UpdateDepartmentRequest request);

    Map<String, Object> deleteDepartment(Long id);

    Map<String, Object> importDepartments(MultipartFile file);
}
