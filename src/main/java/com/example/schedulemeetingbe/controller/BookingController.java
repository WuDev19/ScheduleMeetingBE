package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.booking.UpdateEquipmentBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.CancelBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.CreateBookingRequest;
import com.example.schedulemeetingbe.dto.request.booking.UpdateBookingRequest;
import com.example.schedulemeetingbe.dto.response.booking.BookingResponse;
import com.example.schedulemeetingbe.dto.response.booking.StatusBookingResponse;
import com.example.schedulemeetingbe.service.base.IBookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
                iBookingService.createBooking(request, jwt.getSubject()),
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
            @RequestBody UpdateBookingRequest request) {
        return ApiResponse.success(
                iBookingService.updateBooking(bookingId, request),
                "Cập nhật lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng dặt thêm thiết bị trước khi bắt đầu lịch họp")
    @PatchMapping("/{bookingId}")
    @PreAuthorize("hasAuthority('BOOKING:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Long>>> addEquipmentBooking(
            @PathVariable Long bookingId,
            @RequestBody List<UpdateEquipmentBookingRequest> request) {
        return ApiResponse.success(
                iBookingService.addEquipmentBooking(bookingId, request),
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
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.approveBooking(bookingId, jwt.getClaim(StringCommon.USER_ID)),
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
            @AuthenticationPrincipal Jwt jwt
    ) {
        return ApiResponse.success(
                iBookingService.rejectBooking(bookingId, jwt.getClaim(StringCommon.USER_ID)),
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
            @Valid @RequestBody CancelBookingRequest request
    ) {
        return ApiResponse.success(
                iBookingService.cancelBooking(bookingId, request),
                "Hủy lịch thành công",
                Constants.SUCCESS_CODE
        );
    }

}
