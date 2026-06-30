package com.example.schedulemeetingbe.constant.enums;

public enum EventType {
    USER_REGISTER,
    RESEND_EMAIL,
    RESET_PASSWORD,
    CREATE_USER,
    UPDATE_EMAIL,
    DELETE_AVATAR,
    BOOKING_CANCELLED_BY_MAINTENANCE,
    SEND_EMAIL_CONFIRM_PARTICIPATE,
    SEND_EMAIL_APPROVE_REJECT, //gửi thông báo cho người tạo lịch họp
    SEND_EMAIL_APPROVE_UPDATE, //khi cập nhật được chấp thuận thì gửi cho tất cả người tham gia biết
    CANCEL_BOOKING_BY_REGISTER
}
