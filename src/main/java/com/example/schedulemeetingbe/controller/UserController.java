package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.request.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.UpdateUserRequest;
import com.example.schedulemeetingbe.service.base.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@Tag(name = "Api CRUD User")
@RequiredArgsConstructor
public class UserController {

    private final IUserService iUserService;

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api lấy thông tin chi tiết người dùng")
    @GetMapping("/me/{id}")
    @PreAuthorize("hasAuthority('USER:VIEW')")
    public ResponseEntity<?> getDetailUser(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.getUserDetail(id),
                "Lấy thông tin chi tiết taì khoản thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho admin tạo tài khoản người dùng")
    @PostMapping
    @PreAuthorize("hasAuthority('USER:CREATE')")
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.success(
                iUserService.createUser(request),
                "Tạo tài khoản cho người dùng thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho cập nhật tài khoản người dùng")
    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    public ResponseEntity<?> updateAccount(@PathVariable Long id, @Valid @ModelAttribute UpdateUserRequest request) {
        return ApiResponse.success(
                iUserService.updateUser(id, request),
                "Cập nhật tài khoản thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho cập nhật email mới cho người dùng")
    @PatchMapping("/{id}/email")
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    public ResponseEntity<?> updateEmail(@PathVariable Long id, @RequestParam String newEmail) {
        return ApiResponse.success(
                iUserService.updateEmail(id, newEmail),
                "Cập nhật email mới thành công",
                Constants.SUCCESS_CODE
        );
    }

}