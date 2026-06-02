package com.example.schedulemeetingbe.dto.request;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.*;

public record CreateUserRequest (
        @NotNull(message = StringCommon.NOT_NULL + "username")
        @Size(min = 3, max = 50, message = "Vui lòng nhập từ 3 đến 50 kí tự")
        String username,

        @Email(message = "Email không hợp lệ")
        @NotNull(message = StringCommon.NOT_NULL + "email")
        String email,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Mật khẩu phải từ 8 ký tự trở lên; bao gồm chữ hoa; chữ thường; số và ký tự đặc biệt")
        @NotBlank(message = "Dữ liệu mật khẩu " + StringCommon.NOT_BLANK)
        @NotNull(message = StringCommon.NOT_NULL + "password")
        String password,

        @NotNull(message = StringCommon.NOT_NULL + "fullName")
        @NotBlank(message = "Dữ liệu fullName " + StringCommon.NOT_BLANK)
        String fullName
) {
}
