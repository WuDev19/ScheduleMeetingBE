package com.example.schedulemeetingbe.dto.request.auth;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record SendEmailRequest(
        @Email(message = "Email không hợp lệ")
        @NotNull(message = StringCommon.NOT_NULL + "email")
        String email
) {
}
