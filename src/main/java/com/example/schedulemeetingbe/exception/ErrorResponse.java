package com.example.schedulemeetingbe.exception;

import lombok.Getter;

@Getter
public enum ErrorResponse {
    DATA_INVALID("Dữ liệu gửi lên không hợp lệ, vui lòng xem lại các trường dữ liệu", 1000),
    PASSWORD_NOT_MATCH("Mật khẩu không trùng khớp", 1001),
    NULL_POINTER("Dữ liệu trống", 1002),
    JWT_EXCEPTION("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại", 1003),
    PARSE_JSON("Định dạng dữ liệu không hợp lệ", 1004),
    FIELD_INVALID("Trường dữ liệu gửi lên không hợp lệ", 1005),
    OBJECT_INVALID("", 1006),
    RESOURCE_NOT_FOUND("Dữ liệu không tồn tại", 1007),
    PASSWORD_NOT_TRUE("Mật khẩu không đúng, vui lòng nhập lại", 1008),
    DATA_INTEGRITY("Xung đột dữ liệu", 1009),
    REFRESH_TOKEN_REVOKED("Refresh token đã bị thay đổi, vui lòng đăng nhập lại", 1010),
    VERIFY_TOKEN_EXPIRED("Thời gian xác nhận tài khoản đã hết hạn", 1011),
    VERIFY_TOKEN_REVOKED("Đã xác nhận tài khoản trước đó", 1012);

    private final String message;
    private final int code;

    ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

}

