package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.ApproveRejectRecurringRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.CancelRecurringPatternRequest;
import com.example.schedulemeetingbe.dto.request.recurrence.RecurringPatternFilterRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.ApproveRejectRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.CancelRecurrenceResponse;
import com.example.schedulemeetingbe.dto.response.recurrence.RecurringPatternResponse;
import com.example.schedulemeetingbe.service.base.IRecurringPatternService;
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

@RestController
@RequestMapping("/api/v1/recurring-pattern")
@RequiredArgsConstructor
@Tag(name = "Tài liệu API cho RecurringPattern", description = "Tạo lịch họp định kỳ")
public class RecurringPatternController {

    private final IRecurringPatternService iRecurringPatternService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng đăng kí lịch họp định kì")
    @PostMapping
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:MANAGE')")
    public ResponseEntity<ApiResult<RecurringPatternResponse>> createRecurrenceBooking(
            @Valid @RequestBody RecurringPatternCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                iRecurringPatternService.createRecurring(request, jwt.getClaim(StringCommon.USER_ID)),
                "Đặt lịch định kỳ thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng hủy lịch họp định kì")
    @PatchMapping("/cancel/{recurringId}")
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:MANAGE')")
    public ResponseEntity<ApiResult<CancelRecurrenceResponse>> cancelRecurrenceBooking(
            @PathVariable Long recurringId,
            @RequestBody CancelRecurringPatternRequest request
    ) {
        return ApiResponse.success(
                iRecurringPatternService.cancelRecurring(recurringId, request),
                "Hủy lịch định kỳ thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng xem lịch họp định kì của chính mình")
    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:MANAGE')")
    public ResponseEntity<ApiResult<PageResponse<RecurringPatternResponse>>> getMyRecurringPattern(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRecurringPatternService.getMyRecurringPattern(jwt.getClaim(StringCommon.USER_ID), pageable),
                "Lấy lịch định kỳ của mình thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver duyệt lịch họp định kì")
    @PatchMapping("/approve/{recurringId}")
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:APPROVE')")
    public ResponseEntity<ApiResult<ApproveRejectRecurrenceResponse>> approveOrRejectRecurrenceBooking(
            @PathVariable Long recurringId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody ApproveRejectRecurringRequest request
    ) {
        return ApiResponse.success(
                iRecurringPatternService.approveOrRejectRecurring(
                        recurringId,
                        jwt.getClaim(StringCommon.USER_ID),
                        request
                ),
                "Duyệt lịch định kỳ thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho approver hiển thị lịch họp định kì chưa duyệt")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:VIEW_ALL')")
    public ResponseEntity<ApiResult<PageResponse<RecurringPatternResponse>>> getRecurringPatternWaiting(
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRecurringPatternService.getRecurringPatternWaiting(pageable),
                "Danh sách lịch định kỳ chưa duyệt",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lọc lịch họp định kì chưa duyệt")
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:VIEW')")
    public ResponseEntity<ApiResult<PageResponse<RecurringPatternResponse>>> filter(
            @ModelAttribute RecurringPatternFilterRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault Pageable pageable
    ) {
        return ApiResponse.success(
                iRecurringPatternService.filter(
                        jwt.getClaim(StringCommon.USER_ID),
                        jwt.getClaim(StringCommon.ROLES),
                        request,
                        pageable),
                "Danh sách lịch định kỳ đã lọc",
                Constants.SUCCESS_CODE
        );
    }

}
