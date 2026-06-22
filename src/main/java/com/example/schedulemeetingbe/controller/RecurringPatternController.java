package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.booking.RecurringPatternCreateRequest;
import com.example.schedulemeetingbe.dto.response.booking.RecurringPatternResponse;
import com.example.schedulemeetingbe.service.base.IRecurringPatternService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/recurring-pattern")
@RequiredArgsConstructor
@Tag(name = "Tài liệu API cho RecurringPattern", description = "Tạo lịch họp định kỳ")
public class RecurringPatternController {

    private final IRecurringPatternService iRecurringPatternService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho người dùng đăng kí lịch họp")
    @PostMapping
    @PreAuthorize("hasAuthority('RECURRING_BOOKING:MANAGE')")
    public ResponseEntity<ApiResult<RecurringPatternResponse>> createBooking(
            @Valid @RequestBody RecurringPatternCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        return ApiResponse.success(
                iRecurringPatternService.createRecurring(request, jwt.getClaim(StringCommon.USER_ID)),
                "Đặt lịch định kỳ thành công",
                Constants.SUCCESS_CODE
        );
    }

}
