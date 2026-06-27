package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.department.CreateDepartmentRequest;
import com.example.schedulemeetingbe.dto.request.department.UpdateDepartmentRequest;
import com.example.schedulemeetingbe.dto.response.DepartmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.service.base.IDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/department")
@Tag(name = "Tài liệu API cho Department")
@RequiredArgsConstructor
public class DepartmentController {

    private final IDepartmentService iDepartmentService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin tạo department")
    @PostMapping
    @PreAuthorize("hasAuthority('DEPARTMENT:CREATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> createDepartment(
            @Valid @RequestBody CreateDepartmentRequest request
    ) {
        return ApiResponse.success(
                iDepartmentService.createDepartment(request),
                "Tạo department thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho lấy thông tin chi tiết department")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:VIEW')")
    public ResponseEntity<ApiResult<DepartmentResponse>> getDepartment(@PathVariable Long id) {
        return ApiResponse.success(
                iDepartmentService.getDepartment(id),
                "Lấy thông tin department thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho lấy danh sách department")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('DEPARTMENT:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<DepartmentResponse>>> getDepartments(
            @RequestParam(value = "keyword", required = false) String keyword,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iDepartmentService.getDepartments(keyword, pageable),
                "Lấy danh sách department thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật department")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        return ApiResponse.success(
                iDepartmentService.updateDepartment(id, request),
                "Cập nhật department thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa department")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DEPARTMENT:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteDepartment(@PathVariable Long id) {
        return ApiResponse.success(
                iDepartmentService.deleteDepartment(id),
                "Xóa department thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin import department bằng Excel")
    @PostMapping("/import")
    @PreAuthorize("hasAuthority('DEPARTMENT:CREATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> importDepartments(
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.success(
                iDepartmentService.importDepartments(file),
                "Import department thành công",
                Constants.SUCCESS_CODE
        );
    }
}
