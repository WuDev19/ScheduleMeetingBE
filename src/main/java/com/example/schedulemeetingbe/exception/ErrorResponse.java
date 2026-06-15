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
    VERIFY_TOKEN_REVOKED("Token không còn khả dụng", 1012),
    USER_ALREADY_ACTIVE("Tài khoản đã được kích hoạt, vui lòng đăng nhập", 1013),
    EMAIL_AUTH_ERROR("Sai tài khoản, mật khẩu SMTP", 1014),
    SMTP_CONFIG_ERROR("Cấu hình SMTP không chính xác hoặc sai thông tin đăng nhập", 1015),
    INVALID_RECIPIENT("Địa chỉ email người nhận không hợp lệ hoặc không tồn tại", 1016),
    ATTACHMENT_ERROR("Tệp đính kèm không hợp lệ hoặc vượt quá dung lượng cho phép", 1017),
    SYSTEM_UNKNOWN_ERROR("Gửi email thất bại do lỗi hệ thống chưa xác định", 1018),
    FILE_ACCESS_ERROR("Lỗi không thể truy cập file", 1019),
    FILE_EXCEED_MEMORY("File vượt quá dung lượng cho phép", 1020),
    FAKE_AUTH_ERROR("Bạn đang mạo danh người khác, nghiêm cấm hành vi này", 1021);

    private final String message;
    private final int code;

    ErrorResponse(String message, int code) {
        this.message = message;
        this.code = code;
    }

}

