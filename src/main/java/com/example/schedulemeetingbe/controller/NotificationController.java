package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.notification.CreateNotificationRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.notification.NotificationResponse;
import com.example.schedulemeetingbe.dto.response.notification.UnreadCountResponse;
import com.example.schedulemeetingbe.service.base.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notification")
@Tag(name = "Tài liệu API cho Notification")
@RequiredArgsConstructor
public class NotificationController {

    private final INotificationService iNotificationService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api tạo notification (internal/admin)")
    @PostMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:SEND')")
    public ResponseEntity<ApiResult<Map<String, Object>>> create(
            @RequestBody CreateNotificationRequest request
    ) {
        return ApiResponse.success(
                iNotificationService.createNotification(request),
                "Tạo notification thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy danh sách notification của user hiện tại")
    @GetMapping
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<NotificationResponse>>> getAll(
            @PageableDefault(sort = "createdAt") Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = jwt.getClaim(StringCommon.USER_ID);
        return ApiResponse.success(
                iNotificationService.getNotifications(userId, pageable),
                "Lấy danh sách notification thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api đếm số notification chưa đọc")
    @GetMapping("/unread-count")
    @PreAuthorize("hasAuthority('NOTIFICATION:VIEW')")
    public ResponseEntity<ApiResult<UnreadCountResponse>> countUnread(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iNotificationService.countUnread(jwt.getClaim(StringCommon.USER_ID)),
                "Lấy số notification chưa đọc thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api đánh dấu một notification là đã đọc")
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
    public ResponseEntity<ApiResult<NotificationResponse>> markAsRead(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iNotificationService.markAsRead(notificationId, jwt.getClaim(StringCommon.USER_ID)),
                "Đánh dấu đã đọc thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api đánh dấu tất cả notification là đã đọc")
    @PatchMapping("/read-all")
    @PreAuthorize("hasAuthority('NOTIFICATION:UPDATE')")
    public ResponseEntity<ApiResult<Void>> markAllAsRead(
            @AuthenticationPrincipal Jwt jwt
    ) {
        iNotificationService.markAllAsRead(jwt.getClaim(StringCommon.USER_ID));
        return ApiResponse.success(
                null,
                "Đánh dấu tất cả đã đọc thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api xoá một notification")
    @DeleteMapping("/{notificationId}")
    @PreAuthorize("hasAuthority('NOTIFICATION:DELETE')")
    public ResponseEntity<ApiResult<Void>> delete(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        iNotificationService.deleteNotification(notificationId, jwt.getClaim(StringCommon.USER_ID));
        return ApiResponse.success(
                null,
                "Xoá notification thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api xoá nhiều notification đã chọn")
    @DeleteMapping("/selected")
    @PreAuthorize("hasAuthority('NOTIFICATION:DELETE')")
    public ResponseEntity<ApiResult<Void>> deleteAllSelected(
            @RequestBody List<Long> notificationIds,
            @AuthenticationPrincipal Jwt jwt
    ) {
        iNotificationService.deleteAllNotificationSelected(notificationIds, jwt.getClaim(StringCommon.USER_ID));
        return ApiResponse.success(
                null,
                "Xoá notification thành công",
                Constants.SUCCESS_CODE
        );
    }
}
