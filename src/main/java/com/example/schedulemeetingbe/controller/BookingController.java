package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.booking.*;
import com.example.schedulemeetingbe.dto.request.room.StartEndTimeRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.booking.*;
import com.example.schedulemeetingbe.dto.response.booking.booking_notification.BookingNotificationResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_overlap.BookingOverlapResponse;
import com.example.schedulemeetingbe.dto.response.booking.booking_summary.BookingSummaryResponse;
import com.example.schedulemeetingbe.service.base.IBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/booking")
@RequiredArgsConstructor
@Tag(name = "Tài liệu API cho Booking")
public class BookingController {

    private final IBookingService iBookingService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng đăng kí lịch họp")
    @PostMapping
    @PreAuthorize("hasAuthority('BOOKING:CREATE')")
    public ResponseEntity<ApiResult<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                iBookingService.createBooking(request, jwt.getClaim(StringCommon.USER_ID)),
                "Đặt lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng cập nhật lịch họp")
    @PatchMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> updateBooking(
            @PathVariable Long bookingId,
            @RequestBody UpdateBookingRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                iBookingService.updateBooking(
                        bookingId,
                        request,
                        jwt.getClaim(StringCommon.USER_ID),
                        jwt.getClaim(StringCommon.ROLES)),
                "Cập nhật lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng dặt thêm thiết bị trước khi bắt đầu lịch họp")
    @PostMapping("/{bookingId}/equipment")
    @PreAuthorize("hasAuthority('BOOKING:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> addEquipmentBooking(
            @PathVariable Long bookingId,
            @RequestBody List<UpdateEquipmentBookingRequest> request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                iBookingService.addEquipmentBooking(bookingId, request, jwt.getClaim(StringCommon.USER_ID)),
                "Gửi yêu cầu bổ sung thiết bị thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver duyệt lịch họp")
    @PatchMapping("/approve/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:APPROVE')")
    public ResponseEntity<ApiResult<StatusBookingResponse>> approveBooking(
            @PathVariable Long bookingId,
            @RequestBody ApproveRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.approveBooking(bookingId, request, jwt.getClaim(StringCommon.USER_ID)),
                "Duyệt lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver từ chối lịch họp")
    @PatchMapping("/reject/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:REJECT')")
    public ResponseEntity<ApiResult<StatusBookingResponse>> rejectBooking(
            @PathVariable Long bookingId,
            @RequestBody RollBackRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.rejectBooking(bookingId, request, jwt.getClaim(StringCommon.USER_ID)),
                "Từ chối lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng hủy lịch họp")
    @PatchMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:CANCEL')")
    public ResponseEntity<ApiResult<StatusBookingResponse>> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody CancelBookingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.cancelBooking(bookingId, request, jwt.getClaim(StringCommon.USER_ID)),
                "Hủy lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin xóa lịch họp từ giao diện")
    @DeleteMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteBooking(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.deleteBooking(bookingId, jwt.getClaim(StringCommon.USER_ID)),
                "Xóa lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho register bổ sung người tham gia")
    @PatchMapping("/{bookingId}/participants")
    @PreAuthorize("hasAuthority('BOOKING:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> addParticipants(
            @PathVariable Long bookingId,
            @RequestBody List<String> emails
    ) {
        return ApiResponse.success(
                iBookingService.addParticipants(bookingId, emails),
                "Gửi yêu cầu thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng xem chi tiết lịch họp")
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:VIEW')")
    public ResponseEntity<ApiResult<BookingDetailResponse>> getBookingDetail(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.getBookingDetail(bookingId, jwt.getClaim(StringCommon.USER_ID)),
                "Xem chi tiết lịch họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng cập nhật số lượng thiết bị của lịch họp")
    @PatchMapping("/{bookingId}/equipment/{equipmentId}/{beId}")
    @PreAuthorize("hasAuthority('BOOKING:UPDATE')")
    public ResponseEntity<ApiResult<BookingEquipmentResponse>> updateBookingEquipmentQuantity(
            @PathVariable Long bookingId,
            @PathVariable Long equipmentId,
            @PathVariable Long beId,
            @RequestBody UpdateBookingEquipQuantityRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.updateBookingEquipmentQuantity(
                        bookingId,
                        jwt.getClaim(StringCommon.USER_ID),
                        equipmentId,
                        beId,
                        request
                ),
                "Cập nhật số lượng thiết bị cụ thể của lịch họp thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver xem danh sách lịch họp đang chờ")
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('BOOKING_STATUS:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<BookingSummaryResponse>>> getBookingWaitingApprove(
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iBookingService.getBookingWaitingApprove(pageable),
                "Hiển thị danh sách lịch họp đang chờ thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver xem chi tiết lịch họp đang chờ duyệt")
    @GetMapping("/pending/detail/{historyId}")
    @PreAuthorize("hasAuthority('BOOKING_STATUS:VIEW')")
    public ResponseEntity<ApiResult<BookingHistoryResponse>> getBookingHistoryDetailToApprove(@PathVariable Long historyId) {
        return ApiResponse.success(
                iBookingService.getBookingHistoryDetailToApprove(historyId),
                "Hiển thị chi tiết lịch họp đang chờ duyệt thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver xem chi tiết về lịch họp trong thông báo")
    @GetMapping("/{bookingId}/detail/notification/{notificationId}")
    @PreAuthorize("hasAuthority('BOOKING:VIEW')")
    public ResponseEntity<ApiResult<BookingNotificationResponse>> getBookingAndNotification(
            @PathVariable Long bookingId,
            @PathVariable Long notificationId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.getBookingAndNotification(bookingId, jwt.getClaim(StringCommon.USER_ID), notificationId),
                "Hiển thị chi tiết về lịch họp trong thông báo thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lọc lịch đặt theo thông tin phòng, người đặt, trạng thái và thời gian")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('BOOKING:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<BookingResponse>>> filterBookings(
            @ModelAttribute BookingFilterRequest request,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iBookingService.filterBooking(request, pageable),
                "Lọc lịch đặt thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy lịch đặt theo ngày/tuần/tháng")
    @GetMapping("/view")
    @PreAuthorize("hasAuthority('BOOKING:VIEW')")
    public ResponseEntity<ApiResult<List<BookingResponse>>> viewBookings(@ModelAttribute BookingViewRequest request) {
        return ApiResponse.success(
                iBookingService.viewBookings(request),
                "Lấy lịch đặt theo view thành công",
                Constants.SUCCESS_CODE
        );
    }

    @GetMapping("/attendee/confirm")
    @Operation(summary = "Api cho người dùng xác nhận tham gia lịch họp qua email")
    public String confirmParticipateEmail(
            @RequestParam String token,
            @RequestParam Long bookingId
    ) {
        iBookingService.verifyEmailAndUpsertBookingAttendee(token, bookingId);
        return StringCommon.CONFIRM_PARTICIPATE_HTML;
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng xác nhận tham gia lịch họp qua chỗ thông báo trên web")
    @PostMapping("/{bookingId}/attendee/confirm")
    @PreAuthorize("hasAuthority('BOOKING:CONFIRM')")
    public ResponseEntity<ApiResult<Void>> confirmParticipate(
            @PathVariable Long bookingId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        iBookingService.confirmParticipateIn(bookingId, jwt.getClaim(StringCommon.USER_ID));
        return ApiResponse.success(
                null,
                "Xác nhận tham gia thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Export booking list to Excel")
    @GetMapping("/export")
    @PreAuthorize("hasAuthority('BOOKING:EXPORT')")
    public ResponseEntity<byte[]> exportBookings(
            @ModelAttribute BookingExportRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        byte[] excel = iBookingService.exportBookings(request, jwt.getClaim(StringCommon.USER_ID));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bookings.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(excel);
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy danh sách booking bị trùng khi tạo phòng không khả dụng")
    @GetMapping("/overlap-room-unavailability/{roomId}")
    @PreAuthorize("hasAuthority('ROOM_UNAVAILABLE:MANAGE')")
    public ResponseEntity<ApiResult<List<BookingOverlapResponse>>> getBookingOverlapRoomUnavailability(
            @PathVariable Long roomId,
            @ModelAttribute StartEndTimeRequest request
    ) {
        return ApiResponse.success(
                iBookingService.getBookingOverlapRoomUnavailability(roomId, request),
                "Lấy danh sách booking bị trùng khi tạo phòng không khả dụng thành công",
                Constants.SUCCESS_CODE
        );
    }

}
