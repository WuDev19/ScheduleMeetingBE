package com.example.schedulemeetingbe.dto.request.user;

import com.example.schedulemeetingbe.constant.StringCommon;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(

        @NotBlank(message = "Dữ liệu username " + StringCommon.NOT_BLANK)
        @Size(min = 3, max = 50, message = "Vui lòng nhập từ 3 đến 50 kí tự")
        String username,

        @Email(message = "Email không hợp lệ")
        @NotBlank(message = "Dữ liệu email " + StringCommon.NOT_BLANK)
        String email,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                message = "Mật khẩu phải từ 8 ký tự trở lên; bao gồm chữ hoa; chữ thường; số và ký tự đặc biệt")
        @NotBlank(message = "Dữ liệu mật khẩu " + StringCommon.NOT_BLANK)
        String password,

        @NotBlank(message = "Dữ liệu fullName " + StringCommon.NOT_BLANK)
        String fullName,

        @Pattern(regexp = "^(03|05|07|08|09)\\d{8}$", message = "Số điện thoại không hợp lệ")
        String phone,

        @NotBlank(message = "Dữ liệu role " + StringCommon.NOT_BLANK)
        String role
) {
}
