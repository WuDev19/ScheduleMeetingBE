package com.example.schedulemeetingbe.controller;

import com.example.schedulemeetingbe.constant.Constants;
import com.example.schedulemeetingbe.dto.common.ApiResponse;
import com.example.schedulemeetingbe.dto.common.ApiResult;
import com.example.schedulemeetingbe.dto.request.auth.LoginByUsernameRequest;
import com.example.schedulemeetingbe.dto.request.auth.LogoutRequest;
import com.example.schedulemeetingbe.dto.request.auth.SendEmailRequest;
import com.example.schedulemeetingbe.dto.request.auth.SignUpWithUsernameRequest;
import com.example.schedulemeetingbe.dto.response.LoginResponse;
import com.example.schedulemeetingbe.service.base.IAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(name = "Tài liệu API cho Authentication")
public class AuthController {

    private final IAuthenticationService iAuthenticationService;

    @Operation(summary = "Đăng kí tài khoản bằng username password")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResult<Map<String, Object>>> signUpWithUsername(@Valid @RequestBody SignUpWithUsernameRequest request) {
        return ApiResponse.success(
                iAuthenticationService.signUpWithUsername(request),
                "Đăng kí tài khoản thành công",
                Constants.SUCCESS_CODE
        );
    }

    @Operation(summary = "API dành cho đăng nhập bằng username + password")
    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginByUsernameRequest request) {
        return ApiResponse.success(
                iAuthenticationService.login(request),
                "Đăng nhập thành công",
                Constants.SUCCESS_CODE
        );
    }

    @Operation(summary = "API dành cho đăng xuất khi đăng nhập bằng username")
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Map<String, Object>>> logout(@Valid @RequestBody LogoutRequest request) {
        return ApiResponse.success(
                iAuthenticationService.logout(request),
                "Đăng xuất thành công",
                Constants.SUCCESS_CODE
        );
    }

    @Operation(
            summary = "API dành cho cấp phát lại khi access token hết hiệu lực",
            description = "Cấp lại cả access token và refresh token sau đó cho access token cũ vào trong black list" +
                    " và refresh token cũ update thành revoke"
    )
    @PostMapping("/refresh-token/{refToken}")
    public ResponseEntity<ApiResult<LoginResponse>> refreshToken(@PathVariable String refToken) {
        return ApiResponse.success(
                iAuthenticationService.refreshToken(refToken),
                "Cấp lại access token và refresh token thành công",
                Constants.SUCCESS_CODE
        );
    }

    @Operation(summary = "API dành cho khi người dùng bấm vào link xác nhân đăng kí tài khoản khi gửi qua email")
    @GetMapping("/verify")
    public String verifyEmail(@RequestParam String token) {
        iAuthenticationService.verifyEmail(token);
        return "Verified Successfully";
    }

    @Operation(summary = "API dành cho khi người dùng bấm vào link xác nhân cập nhật email mới khi gửi qua email")
    @GetMapping("/verify/new-email")
    public String verifyNewEmail(@RequestParam String token, @RequestParam String email) {
        iAuthenticationService.verifyUpdateEmail(token, email);
        return "Verified Successfully";
    }

    @Operation(summary = "API gửi lại link verify account")
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResult<Map<String, Object>>> resendEmailVerifyAccount(@Valid @RequestBody SendEmailRequest request) {
        return ApiResponse.success(
                iAuthenticationService.resendEmailVerifyAccount(request),
                "Gửi lại link xác nhận thành công",
                Constants.SUCCESS_CODE
        );
    }

    @Operation(summary = "API cho quên mật khẩu", description = "Người dùng bấm quên mật khẩu sau đó nhập email đẵ đăng kí để nhận link mật khẩu mới")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResult<Map<String, Object>>> forgotPassword(@Valid @RequestBody SendEmailRequest request) {
        return ApiResponse.success(
                iAuthenticationService.sendEmailResetPassword(request),
                "Gửi email reset mật khẩu thành công",
                Constants.SUCCESS_CODE
        );
    }

}
