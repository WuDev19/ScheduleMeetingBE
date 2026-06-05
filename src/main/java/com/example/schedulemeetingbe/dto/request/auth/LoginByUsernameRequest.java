package com.example.schedulemeetingbe.dto.request.auth;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.*;

public record LoginByUsernameRequest(
        @Size(min = 1, message = "Vui lòng nhập username")
        @NotNull
        String username,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Mật khẩu phải từ 8 ký tự trở lên, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
        @NotBlank(message = "Mật khẩu " + StringCommon.NOT_BLANK)
        @NotNull
        String password
) {
}
