package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.department.CreateDepartmentRequest;
import com.example.schedulemeetingbe.dto.request.department.UpdateDepartmentRequest;
import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.entity.Department;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.DepartmentMapper;
import com.example.schedulemeetingbe.repository.DepartmentRepository;
import com.example.schedulemeetingbe.service.base.IDepartmentService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements IDepartmentService {

    private final DepartmentRepository departmentRepository;

    @Transactional
    @Override
    public Map<String, Object> createDepartment(CreateDepartmentRequest request) {
        if (departmentRepository.existsByDepartmentName(request.departmentName())) {
            throw new BusinessException(ErrorResponse.DATA_INTEGRITY);
        }
        if (departmentRepository.existsByDepartmentCode(request.departmentCode())) {
            throw new BusinessException(ErrorResponse.DATA_INTEGRITY);
        }
        Department department = Department.builder()
                .departmentName(request.departmentName())
                .departmentCode(request.departmentCode())
                .description(request.description())
                .build();
        departmentRepository.save(department);
        return CRUDResponseHelper.createSuccess();
    }

    @Override
    public DepartmentResponse getDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        return DepartmentMapper.mapToDepartmentResponse(department);
    }

    @Override
    public PageResponse<DepartmentResponse> getDepartments(String keyword, Pageable pageable) {
        Page<Department> departmentPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            departmentPage = departmentRepository.searchDepartments(keyword.trim(), pageable);
        } else {
            departmentPage = departmentRepository.findByDeletedAtIsNull(pageable);
        }
        return new PageResponse<>(
                departmentPage.getNumber(),
                departmentPage.getNumberOfElements(),
                departmentPage.getTotalElements(),
                departmentPage.getTotalPages(),
                departmentPage.getContent().stream()
                        .map(DepartmentMapper::mapToDepartmentResponse)
                        .toList()
        );
    }

    @Transactional
    @Override
    public Map<String, Object> updateDepartment(Long id, UpdateDepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        if (request.departmentName() != null && !request.departmentName().equals(department.getDepartmentName())) {
            if (departmentRepository.existsByDepartmentName(request.departmentName())) {
                throw new BusinessException(ErrorResponse.DATA_INTEGRITY);
            }
            department.setDepartmentName(request.departmentName());
        }
        if (request.departmentCode() != null && !request.departmentCode().equals(department.getDepartmentCode())) {
            if (departmentRepository.existsByDepartmentCode(request.departmentCode())) {
                throw new BusinessException(ErrorResponse.DATA_INTEGRITY);
            }
            department.setDepartmentCode(request.departmentCode());
        }
        if (request.description() != null) {
            department.setDescription(request.description());
        }
        return CRUDResponseHelper.updateSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> deleteDepartment(Long id) {
        Department department = departmentRepository.findById(id)
                .filter(d -> d.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        department.setDeletedAt(TimeUtils.now());
        return CRUDResponseHelper.deleteSuccess();
    }

    @Transactional
    @Override
    public Map<String, Object> importDepartments(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorResponse.DATA_INVALID);
        }
        List<Map<String, String>> errors = new ArrayList<>();
        List<Department> departments = new ArrayList<>();
        Map<String, Integer> seenCodes = new HashMap<>();
        Map<String, Integer> seenNames = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new BusinessException(ErrorResponse.DATA_INVALID);
            }
            int rowNumber = 0;
            for (Row row : sheet) {
                if (rowNumber++ == 0) {
                    continue;
                }
                String departmentName = row.getCell(0) != null ? row.getCell(0).toString().trim() : "";
                String departmentCode = row.getCell(1) != null ? row.getCell(1).toString().trim() : "";
                String description = row.getCell(2) != null ? row.getCell(2).toString().trim() : null;
                if (departmentName.isBlank() && departmentCode.isBlank()) {
                    continue;
                }
                if (departmentName.isBlank()) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Tên phòng ban không được để trống"));
                    continue;
                }
                if (departmentCode.isBlank()) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Mã phòng ban không được để trống"));
                    continue;
                }
                String codeKey = departmentCode.toLowerCase();
                String nameKey = departmentName.toLowerCase();
                if (seenCodes.containsKey(codeKey)) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Mã phòng ban bị trùng trong file"));
                    continue;
                }
                if (seenNames.containsKey(nameKey)) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Tên phòng ban bị trùng trong file"));
                    continue;
                }
                if (departmentRepository.existsByDepartmentCode(departmentCode)) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Mã phòng ban đã tồn tại"));
                    continue;
                }
                if (departmentRepository.existsByDepartmentName(departmentName)) {
                    errors.add(Map.of("row", String.valueOf(rowNumber), "error", "Tên phòng ban đã tồn tại"));
                    continue;
                }
                seenCodes.put(codeKey, rowNumber);
                seenNames.put(nameKey, rowNumber);
                departments.add(Department.builder()
                        .departmentName(departmentName)
                        .departmentCode(departmentCode)
                        .description(description)
                        .build());
            }
            departmentRepository.saveAll(departments);
            return Map.of(
                    "imported", departments.size(),
                    "errors", errors
            );
        } catch (IOException ex) {
            throw new BusinessException(ErrorResponse.FILE_ACCESS_ERROR);
        }
    }
}
