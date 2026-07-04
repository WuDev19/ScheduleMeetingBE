package com.example.schedulemeetingbe.constant;

public class StringCommon {
    private StringCommon() {
    }

    public static final String APP_NAME_UPPER_CASE = "HỆ THỐNG QUẢN LÝ PHÒNG HỌP VÀ ĐĂNG KÝ LỊCH SỬ DỤNG";
    public static final String APP_NAME_LOWER_CASE = "Hệ thống quản lý phòng họp và đăng ký lịch sử dụng";
    public static final String BASE_URL_APP = " https://nonliberal-blossom-unkilned.ngrok-free.dev";
    public static final String AUTHORIZATION = "Authorization";
    public static final String SECURITY_SCHEME = "bearerAuth";
    public static final String TOKEN_IN_BLACKLIST = "Token đang trong danh sách đen";
    public static final String USER_ID = "userId";
    public static final String PERMISSIONS = "permissions";
    public static final String ROLES = "roles";
    public static final String TIME_ZONE_VN = "Asia/Ho_Chi_Minh";
    public static final String SUCCESS = "Success";
    public static final String ERROR = "Error";
    public static final String MODIFY_AT = "modifiedAt";
    public static final String DELETED_AT = "deletedAt";
    public static final String CREATED_AT = "createdAt";
    public static final String UPDATED_AT = "updatedAt";
    public static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss Z";
    public static final String DATE_TIME_FORMAT_NO_TZ = "dd-MM-yyyy HH:mm:ss";
    public static final String OFFSET_FORMAT = "yyyy-MM-dd HH:mm:ss[XXX][X]";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String NOT_BLANK = "không được để trống";
    public static final String NOT_NULL = "Thiếu dữ liệu trường ";
    public static final String ADMIN = "ADMIN";
    public static final String APPROVER = "APPROVER";
    public static final String REGISTER = "REGISTER";
    public static final String TITLE_NOTIFICATION_EMAIL = "Thông báo về lịch họp đã đăng ký";
    public static final String TITLE_NOTIFICATION = "Thông báo lịch họp";
    public static final String TITLE_NOTIFICATION_CANCEL_BOOKING = "THÔNG BÁO HỦY LỊCH HỌP";
    public static final String CONFIRM_PARTICIPATE_HTML = """
            <!DOCTYPE html>
            <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <title>Xác Nhận Tham Gia Họp</title>
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f6f9; text-align: center; padding: 50px; }
                    .card { background: white; padding: 40px; border-radius: 12px; display: inline-block; box-shadow: 0 4px 15px rgba(0,0,0,0.1); max-width: 450px; }
                    .icon { font-size: 50px; color: #2ecc71; margin-bottom: 20px; }
                    h2 { color: #2c3e50; margin-bottom: 10px; }
                    p { color: #7f8c8d; font-size: 16px; line-height: 1.5; }
                </style>
            </head>
            <body>
                <div class="card">
                    <div class="icon">✔</div>
                    <h2>Xác Nhận Thành Công!</h2>
                    <p>Cảm ơn bạn đã phản hồi. Hệ thống đã ghi nhận bạn sẽ tham gia cuộc họp này và cập nhật vào lịch cá nhân.</p>
                </div>
            </body>
            </html>
            """;

}
