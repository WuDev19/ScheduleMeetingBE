package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.unavailability_room.CreateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UnavailabilityRoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.unavailability_room.UpdateUnavailabilityRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.UnavailabilityRoomResponse;
import com.example.schedulemeetingbe.service.base.IUnavailabilityRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/unavailability-room")
@Tag(name = "Tài liệu API cho Unavailability Room")
@RequiredArgsConstructor
public class RoomUnavailabilityController {

    private final IUnavailabilityRoomService iUnavailabilityRoomService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin thêm phòng họp không khả dụng")
    @PostMapping
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:MANAGE')")
    public ResponseEntity<ApiResult<UnavailabilityRoomResponse>> createRoom(@Valid @RequestBody CreateUnavailabilityRoomRequest request) {
        return ApiResponse.success(
                iUnavailabilityRoomService.create(request),
                "Thêm phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin sửa phòng không khả dụng")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:MANAGE')")
    public ResponseEntity<ApiResult<UnavailabilityRoomResponse>> updateRoom(
            @PathVariable Long id,
            @RequestBody UpdateUnavailabilityRoomRequest request
    ) {
        return ApiResponse.success(
                iUnavailabilityRoomService.update(id, request),
                "Thêm phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa vĩnh viễn phòng không khả dụng")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:MANAGE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteRoom(@PathVariable Long id) {
        return ApiResponse.success(
                iUnavailabilityRoomService.delete(id),
                "Xóa phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa mềm phòng không khả dụng, phục vụ cho việc quản lý về sau, nếu cần có thể xem lại")
    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:MANAGE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> softDeleteRoom(@PathVariable Long id) {
        return ApiResponse.success(
                iUnavailabilityRoomService.softDelete(id),
                "Xóa phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy chi tiết phòng họp không khả dụng")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:VIEW')")
    public ResponseEntity<ApiResult<UnavailabilityRoomResponse>> getDetail(@PathVariable Long id) {
        return ApiResponse.success(
                iUnavailabilityRoomService.getDetail(id),
                "Lấy thông tin phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy chi tiết phòng họp không khả dụng")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<UnavailabilityRoomResponse>>> getAll(
            @RequestParam(required = false) Boolean isDeleted,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iUnavailabilityRoomService.getAll(isDeleted, pageable, jwt.getClaim(StringCommon.ROLES)),
                "Lấy danh sách phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy chi tiết phòng họp không khả dụng")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<UnavailabilityRoomResponse>>> search(
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam String keyword,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iUnavailabilityRoomService.search(isDeleted, keyword, pageable, jwt.getClaim(StringCommon.ROLES)),
                "Tìm kiếm danh sách phòng họp không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy chi tiết phòng họp không khả dụng")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<UnavailabilityRoomResponse>>> filter(
            @RequestParam(required = false) Boolean isDeleted,
            @ModelAttribute UnavailabilityRoomFilterRequest request,
            @PageableDefault Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iUnavailabilityRoomService.filter(isDeleted, request, pageable, jwt.getClaim(StringCommon.ROLES)),
                "Lọc danh sách phòng họp không khả dụng theo thời gian thành công",
                Constants.SUCCESS_CODE
        );
    }

}
