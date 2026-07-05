package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.department.CreateDepartmentRequest;
import com.example.schedulemeetingbe.dto.request.department.UpdateDepartmentRequest;
import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.entity.Department;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.listener.DepartmentImportListener;
import com.example.schedulemeetingbe.mapper.DepartmentMapper;
import com.example.schedulemeetingbe.repository.DepartmentRepository;
import com.example.schedulemeetingbe.service.base.IDepartmentService;
import com.example.schedulemeetingbe.utils.TimeUtils;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelAnalysisException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
        try {
            DepartmentImportListener listener = new DepartmentImportListener(departmentRepository);
            EasyExcel.read(file.getInputStream(), com.example.schedulemeetingbe.dto.excel.DepartmentImportRow.class, listener)
                    .headRowNumber(1)
                    .sheet(0)
                    .doRead();
            List<Department> validDepartments = listener.getValidDepartments();
            departmentRepository.saveAll(validDepartments);
            return Map.of(
                    "imported", validDepartments.size(),
                    "errors", listener.getErrors()
            );
        } catch (ExcelAnalysisException ex) {
            throw new BusinessException(ErrorResponse.DATA_INVALID);
        } catch (IOException ex) {
            throw new BusinessException(ErrorResponse.FILE_ACCESS_ERROR);
        }
    }
}
