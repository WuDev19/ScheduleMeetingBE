package com.example.schedulemeetingbe.dto.request.auth;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LogoutRequest(
        @NotNull(message = StringCommon.NOT_NULL + "accessToken")
        @NotBlank(message = "Dữ liệu accessToken " + StringCommon.NOT_BLANK)
        String accessToken,

        @NotNull(message = StringCommon.NOT_NULL + "refreshToken")
        @NotBlank(message = "Dữ liệu refreshToken " + StringCommon.NOT_BLANK)
        String refreshToken
) {
}
