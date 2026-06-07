package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.user.CreateUserRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateAvatarRequest;
import com.example.schedulemeetingbe.dto.request.user.UpdateUserRequest;
import com.example.schedulemeetingbe.dto.response.UploadSignatureResponse;
import com.example.schedulemeetingbe.dto.response.UserDetailResponse;
import com.example.schedulemeetingbe.service.base.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<ApiResult<UserDetailResponse>> getDetailUser(@PathVariable Long id) {
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
    public ResponseEntity<ApiResult<Map<String, Object>>> createAccount(@Valid @RequestBody CreateUserRequest request) {
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
    public ResponseEntity<ApiResult<UserDetailResponse>> updateUser(@PathVariable Long id, @Valid @ModelAttribute UpdateUserRequest request) {
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
    public ResponseEntity<ApiResult<Map<String, Object>>> updateEmail(@PathVariable Long id, @RequestParam String newEmail) {
        return ApiResponse.success(
                iUserService.updateEmail(id, newEmail),
                "Cập nhật email mới thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho xóa mềm tài khoản")
    @PatchMapping("/lock/{id}")
    @PreAuthorize("hasAuthority('USER:LOCK')")
    public ResponseEntity<ApiResult<Map<String, Object>>> lockAccount(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.lockAccount(id),
                "Xóa tài khoản thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho khôi phục lại tài khoản")
    @PatchMapping("/unlock/{id}")
    @PreAuthorize("hasAuthority('USER:UNLOCK')")
    public ResponseEntity<ApiResult<Map<String, Object>>> unlockAccount(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.unlockAccount(id),
                "Khôi phục tài khoản thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho xóa vĩnh viễn tài khoản")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER:DELETE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteAccount(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.deleteForever(id),
                "Xóa tài khoản vĩnh viễn thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho tạo chữ kí để client upload lên cloudinary")
    @PostMapping("/{id}/avatar/upload-signature")
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    public ResponseEntity<ApiResult<UploadSignatureResponse>> generateUploadSignature(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.generateUploadSignature(id),
                "Tạo chữ ký thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho cập nhật avatar")
    @PatchMapping("/{id}/avatar/upload")
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> updateAvatar(@PathVariable Long id, @RequestBody UpdateAvatarRequest request) {
        return ApiResponse.success(
                iUserService.updateAvatar(id, request),
                "Cập nhật avatar thành công",
                Constants.SUCCESS_CODE
        );
    }

    @SecurityRequirement(name = StringCommon.SECURITY_SCHEME)
    @Operation(summary = "Api cho xóa avatar")
    @DeleteMapping("/{id}/avatar")
    @PreAuthorize("hasAuthority('USER:UPDATE')")
    public ResponseEntity<ApiResult<Map<String, Object>>> deleteAvatar(@PathVariable Long id) {
        return ApiResponse.success(
                iUserService.deleteAvatar(id),
                "Xóa avatar thành công",
                Constants.SUCCESS_CODE
        );
    }

}