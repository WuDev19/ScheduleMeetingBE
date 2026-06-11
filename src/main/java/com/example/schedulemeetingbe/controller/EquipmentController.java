package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.equipment.CreateEquipmentRequest;
import com.example.schedulemeetingbe.dto.request.equipment.UpdateEquipmentRequest;
import com.example.schedulemeetingbe.dto.response.EquipmentResponse;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.service.base.IEquipmentService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/v1/equipment")
@RequiredArgsConstructor
@Tag(name = "Tài liệu API cho Equipment")
public class EquipmentController {

    private final IEquipmentService iEquipmentService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin tạo thiết bị")
    @PostMapping
    @PreAuthorize("hasAuthority('EQUIPMENT:CREATE')")
    public ResponseEntity<ApiResult<EquipmentResponse>> create(@Valid @RequestBody CreateEquipmentRequest request) {
        return ApiResponse.success(
                iEquipmentService.create(request),
                "Tạo thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thiết bị")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:UPDATE')")
    public ResponseEntity<ApiResult<EquipmentResponse>> update(
            @PathVariable Long id,
            @RequestBody UpdateEquipmentRequest request
    ) {
        return ApiResponse.success(
                iEquipmentService.update(id, request),
                "Cập nhật thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thiết bị")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> delete(@PathVariable Long id) {
        return ApiResponse.success(
                iEquipmentService.delete(id),
                "Xóa thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thiết bị")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('EQUIPMENT:VIEW')")
    public ResponseEntity<ApiResult<EquipmentResponse>> getDetail(@PathVariable Long id) {
        return ApiResponse.success(
                iEquipmentService.getDetail(id),
                "Lấy thông tin chi tiết thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thiết bị")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('EQUIPMENT:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<EquipmentResponse>>> getAll(
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iEquipmentService.getAll(pageable),
                "Lấy danh sách thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thiết bị")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('EQUIPMENT:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<EquipmentResponse>>> search(
            @RequestParam String keyword,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iEquipmentService.search(keyword, pageable),
                "Tìm kiếm thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

}
