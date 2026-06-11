package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.equipment.RoomEquipmentQuantityRequest;
import com.example.schedulemeetingbe.dto.request.room.CreateRoomRequest;
import com.example.schedulemeetingbe.dto.request.room.RoomFilterRequest;
import com.example.schedulemeetingbe.dto.request.room.UpdateRoomRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.RoomResponse;
import com.example.schedulemeetingbe.service.base.IRoomService;
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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/room")
@Tag(name = "Tài liệu API cho Room")
@RequiredArgsConstructor
public class RoomController {

    private final IRoomService iRoomService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin tạo thông tin phòng họp")
    @PostMapping
    @PreAuthorize("hasAuthority('ROOM:CREATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ApiResponse.success(
                iRoomService.createRoom(request),
                "Tạo phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thông tin phòng họp")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> updateRoom(@PathVariable Long id, @RequestBody UpdateRoomRequest request) {
        return ApiResponse.success(
                iRoomService.updateRoom(id, request),
                "Cập nhật phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa mềm phòng họp")
    @DeleteMapping("/soft/{id}")
    @PreAuthorize("hasAuthority('ROOM:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> softDeleteRoom(@PathVariable Long id) {
        return ApiResponse.success(
                iRoomService.softDeleteRoom(id),
                "Xóa phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin cập nhật thông tin phòng họp")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteRoom(@PathVariable Long id) {
        return ApiResponse.success(
                iRoomService.deleteRoom(id),
                "Xóa phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho xem thông tin chi tiết phòng họp")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROOM:VIEW')")
    public ResponseEntity<ApiResult<RoomResponse>> getRoomDetail(@PathVariable Long id) {
        return ApiResponse.success(
                iRoomService.getDetail(id),
                "Trả về thông tin chi tiết phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy tất cả phòng họp")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROOM:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<RoomResponse>>> getAll(
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRoomService.getRooms(pageable),
                "Lấy danh sách phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lọc phòng họp theo sức chứa với tầng")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('ROOM:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<RoomResponse>>> filterRoom(
            @ModelAttribute RoomFilterRequest request,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRoomService.filter(request, pageable),
                "Lấy danh sách phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api tìm kiếm phòng họp")
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROOM:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<RoomResponse>>> searchRoom(
            @RequestParam String keyword,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRoomService.search(keyword, pageable),
                "Lấy danh sách phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api thêm thiết bị vào phòng họp")
    @PostMapping("/{roomId}/equipment")
    @PreAuthorize("hasAuthority('ROOM:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> addEquipment(
            @PathVariable Long roomId,
            @RequestBody List<RoomEquipmentQuantityRequest> requests
            ) {
        return ApiResponse.success(
                iRoomService.addEquipmentToRoom(roomId, requests),
                "Thêm thiết bị vào phòng họp thành công",
                Constants.SUCCESS_CODE
        );
    }

}
