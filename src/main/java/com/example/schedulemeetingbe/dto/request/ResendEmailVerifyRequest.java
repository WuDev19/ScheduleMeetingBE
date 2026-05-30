package com.example.schedulemeetingbe.dto.request;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record ResendEmailVerifyRequest(
        @Email(message = "Email không hợp lệ")
        @NotNull(message = StringCommon.NOT_NULL + "email")
        String email
) {
}
