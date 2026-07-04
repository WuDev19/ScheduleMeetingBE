package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.response.BuildingResponse;
import com.example.schedulemeetingbe.dto.request.building.CreateBuildingRequest;
import com.example.schedulemeetingbe.dto.request.building.UpdateBuildingRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.service.base.IBuildingService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/building")
@Tag(name = "Tài liệu API cho Room")
@RequiredArgsConstructor
public class BuildingController {

    private final IBuildingService iBuildingService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin tạo thông tin building")
    @PostMapping
    @PreAuthorize("hasAuthority('BUILDING:CREATE')")
    public ResponseEntity<ApiResult<BuildingResponse>> createBuilding(@Valid @RequestBody CreateBuildingRequest request) {
        return ApiResponse.success(
                iBuildingService.createBuilding(request),
                "Tạo building thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thông tin building")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING:UPDATE')")
    public ResponseEntity<ApiResult<BuildingResponse>> updateBuilding(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBuildingRequest request
    ) {
        return ApiResponse.success(
                iBuildingService.updateBuilding(id, request),
                "Cập nhật building thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa thông tin building")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteBuilding(@PathVariable Long id) {
        return ApiResponse.success(
                iBuildingService.deleteBuilding(id),
                "Xóa building thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho lấy danh sách các building")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('BUILDING:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<BuildingResponse>>> getAllBuilding(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                iBuildingService.getBuildings(page, size),
                "Lấy danh sách building thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho search building")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('BUILDING:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<BuildingResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(
                iBuildingService.search(keyword, page, size),
                "Tìm kiếm danh sách building thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho lấy thông tin chi tiết building")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('BUILDING:VIEW')")
    public ResponseEntity<ApiResult<BuildingResponse>> getAllBuilding(
            @PathVariable Long id
    ) {
        return ApiResponse.success(
                iBuildingService.getDetail(id),
                "Lấy thông tin chi tiết building thành công",
                Constants.SUCCESS_CODE
        );
    }


}
