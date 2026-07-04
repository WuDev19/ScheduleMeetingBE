package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.*;
import com.example.schedulemeetingbe.entity.payload.*;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.VerificationTokenRepository;
import com.example.schedulemeetingbe.service.base.*;
import com.example.schedulemeetingbe.utils.EmailErrorParser;
import com.example.schedulemeetingbe.utils.TimeUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;
    private final IUserService iUserService;
    private final INotificationService iNotificationService;
    private final IRoomService iRoomService;
    private final IBookingService iBookingService;

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${APP_EMAIL}")
    private String APP_EMAIL;

    private static final String UTF8 = "UTF-8";

    @Override
    public void sendEmailActiveAccount(String email, String token) {
        String verifyUrl = StringCommon.BASE_URL_APP + "/api/v1/auth/verify?token=" + token;
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject(StringCommon.APP_NAME_UPPER_CASE);
        simpleMailMessage.setText("Chạm vào link này để kích hoạt tài khoản:\n"
                + verifyUrl);
        javaMailSender.send(simpleMailMessage);
    }

    @Override
    public void sendEmailResetPassword(String email) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int length = 10;
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        String newPassword = sb.toString() + random.nextInt(1000);
        iUserService.updatePassword(email, newPassword);
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            String html = """
                    <div style="font-family: Arial, sans-serif;">
                        <p>Mật khẩu mới của quý khách là:</p>
                    
                        <div style="
                                padding:12px;
                                background:#f4f4f4;
                                border:1px solid #ccc;
                                border-radius:8px;
                                font-size:20px;
                                font-weight:bold;
                                color:#d32f2f;
                                width:fit-content;
                                user-select:all;
                        ">
                            %s
                        </div>
                    
                        <p style="margin-top:16px;">
                            Vui lòng đăng nhập sau đó đổi mật khẩu ngay.
                        </p>
                    </div>
                    """.formatted(newPassword);
            helper.setTo(email);
            helper.setSubject(StringCommon.APP_NAME_UPPER_CASE);
            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException(EmailErrorParser.parseException(e));
        }
    }

    @Override
    public void sendEmailUsernamePassword(String email, String username, String password) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, UTF8);
            String html = """
                    <div style="font-family: Arial, sans-serif;">
                        <p>Thông tin đăng nhập của quý khách:</p>
                    
                        <div style="
                                padding:12px;
                                background:#f4f4f4;
                                border:1px solid #ccc;
                                border-radius:8px;
                         ">
                            <p style="margin:0;">
                                <strong>Tên đăng nhập:</strong>
                                <span style="
                                        font-size:20px;
                                        font-weight:bold;
                                        color:#d32f2f;
                                ">
                                    %s
                                </span>
                            </p>
                    
                            <p style="margin:8px 0 0 0;">
                                <strong>Mật khẩu:</strong>
                                <span style="
                                        font-size:20px;
                                        font-weight:bold;
                                        color:#d32f2f;
                                ">
                                    %s
                                </span>
                            </p>
                        </div>
                    
                        <p style="margin-top:16px;">
                            Vui lòng đăng nhập và đổi mật khẩu ngay sau lần đăng nhập đầu tiên.
                        </p>
                    </div>
                    """.formatted(username, password);
            helper.setTo(email);
            helper.setSubject(StringCommon.APP_NAME_UPPER_CASE);
            helper.setText(html, true);
            javaMailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new BusinessException(EmailErrorParser.parseException(e));
        }
    }

    @Override
    public void sendEmailUpdateEmail(String newEmail, String token) {
        String verifyUrl = StringCommon.BASE_URL_APP +
                "/api/v1/auth/verify/new-email?token=" +
                token +
                "&" +
                "email=" +
                newEmail;
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(newEmail);
        simpleMailMessage.setSubject(StringCommon.APP_NAME_UPPER_CASE);
        simpleMailMessage.setText("Chạm vào link này để xác nhận email thay đổi:\n"
                + verifyUrl);
        javaMailSender.send(simpleMailMessage);
    }

    @Override
    public void sendEmailCancelledBookingByMaintain(BookingCancelledByMaintenancePayload payload) {
        Long userId = payload.userId();
        Long roomId = payload.roomId();
        Long bookingId = payload.bookingId();
        User user = iUserService.getDetail(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Room room = iRoomService.getRoomDetail(roomId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Booking booking = iBookingService.getBooking(bookingId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, UTF8);
            String content = generateContentBookingCancelledByMaintain(user, room, booking, payload.reason());
            mimeMessageHelper.setTo(user.getEmail());
            mimeMessageHelper.setSubject(StringCommon.APP_NAME_UPPER_CASE);
            mimeMessageHelper.setText(content, true);
            javaMailSender.send(mimeMessage);
        } catch (Exception e) {
            EmailErrorParser.parseException(e);
        }
    }

    private String generateContentBookingCancelledByMaintain(User user, Room room, Booking booking, String reason) {
        String startTimeStr = booking.getStartTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
        String endTimeStr = booking.getEndTime().format(DateTimeFormatter.ofPattern(StringCommon.DATE_TIME_FORMAT));
        return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #eee; border-radius: 5px;">
                    <h2 style="color: #d9534f;">Thông Báo Hủy Lịch Họp Đột Xuất</h2>
                    <p>Xin chào <strong>%s</strong>,</p>
                    <p>Chúng tôi rất tiếc phải thông báo rằng lịch đặt phòng của bạn đã bị hủy do sự cố phòng đột xuất.</p>
                
                    <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                        <tr style="background-color: #f8f9fa;">
                            <td style="padding: 10px; border: 1px solid #ddd; font-weight: bold;">Cuộc họp:</td>
                            <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #ddd; font-weight: bold;">Phòng họp:</td>
                            <td style="padding: 10px; border: 1px solid #ddd;">%s</td>
                        </tr>
                        <tr style="background-color: #f8f9fa;">
                            <td style="padding: 10px; border: 1px solid #ddd; font-weight: bold;">Thời gian:</td>
                            <td style="padding: 10px; border: 1px solid #ddd;">Từ %s đến %s</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #ddd; font-weight: bold; color: #d9534f;">Lý do bảo trì:</td>
                            <td style="padding: 10px; border: 1px solid #ddd; color: #d9534f; font-style: italic;">%s</td>
                        </tr>
                    </table>
                
                    <p>Rất mong bạn thông cảm cho sự cố bất tiện này. Vui lòng kiểm tra lại hệ thống để đặt lại lịch họp mới.</p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;" />
                    <p style="font-size: 12px; color: #777;">Đây là email tự động từ Hệ thống Quản lý Phòng họp.</p>
                </div>
                """.formatted(
                user.getFullName(),
                booking.getTitle(),
                room.getRoomName(),
                startTimeStr,
                endTimeStr,
                reason
        );
    }

    /* giả sử đang gửi đc 50/100 người thì server sập khi đó verification_token chưa được lưu
     * -> sau khi retry tạo mới, dữ liệu cũ (rác) ko có trong db -> hợp lí hơn
     * trường hợp những người đã nhận được mail, và bấm xác nhận sẽ check bên db
     * -> khi đó thông báo link email ko còn hợp lệ, vui lòng chờ hệ thống gửi lại
     * giả sử có 100 mail gửi mỗi cái 3s thì hết 5p, sau 5p thì mới save verification
     * */
    //gửi email cho người tham gia xác nhận
    @Override
    public void sendBulkEmailBookingContent(ReceiverEmailPayload payload) {
        Booking booking = iBookingService.getBooking(payload.bookingId())
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Map<String, User> userMap = iUserService.getUserEmailIn(payload.receivers())
                .stream()
                .collect(Collectors.toMap(User::getEmail, Function.identity()));
        List<Notification> notifications = new ArrayList<>();
        List<VerificationToken> verificationTokens = new ArrayList<>();
        payload.receivers().forEach(email -> {
            String token = UUID.randomUUID().toString();
            VerificationToken verificationToken = VerificationToken
                    .builder()
                    .token(token)
                    .expiresAt(TimeUtils.now().plusHours(5))
                    .user(userMap.get(email))
                    .build();
            String mess = """
                    Bạn được mời tham gia cuộc họp "%s".
                    
                    Phòng: %s
                    Thời gian: %s - %s
                    
                    Nhấn để xem chi tiết.
                    """.formatted(
                    payload.title(),
                    payload.room(),
                    payload.startTime(),
                    payload.endTime()
            );
            Notification notification = Notification.builder()
                    .user(userMap.get(email))
                    .title(StringCommon.TITLE_NOTIFICATION)
                    .message(mess)
                    .booking(booking)
                    .build();
            notifications.add(notification);
            verificationTokens.add(verificationToken);
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setTo(email); // Gửi riêng cho từng người
                helper.setSubject(StringCommon.APP_NAME_UPPER_CASE);
                helper.setText(content(payload, token), true);

                javaMailSender.send(message);
            } catch (Exception e) {
                EmailErrorParser.parseException(e);
            }
        });
        iNotificationService.save(notifications);
        verificationTokenRepository.saveAll(verificationTokens);
    }

    private String content(ReceiverEmailPayload payload, String token) {
        String actionUrl = StringCommon.BASE_URL_APP
                + "/api/v1/booking/attendee/confirm?token="
                + token
                + "&bookingId="
                + payload.bookingId();
        String html = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Thông Báo Mời Họp</title>
                </head>
                
                <body style="margin:0;padding:0;background-color:#f1f5f9;font-family:'Segoe UI',Arial,sans-serif;">
                
                <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                       style="background-color:#f1f5f9;padding:30px 10px;">
                    <tr>
                        <td align="center">
                
                            <table width="600" cellpadding="0" cellspacing="0" border="0"
                                   style="max-width:600px;background:#ffffff;border-radius:16px;overflow:hidden;box-shadow:0 4px 12px rgba(0,0,0,0.05);">
                
                                <tr>
                                    <td align="center" style="background:#1e3a8a;padding:35px 20px;">
                                        <h1 style="margin:0;color:#ffffff;font-size:24px;font-weight:700;">
                                            📅 Lời Mời Tham Gia Cuộc Họp
                                        </h1>
                                    </td>
                                </tr>
                
                                <tr>
                                    <td style="padding:40px 30px;">
                
                                        <p style="margin:0 0 16px;font-size:16px;font-weight:600;color:#0f172a;">
                                            Xin chào Anh/Chị,
                                        </p>
                
                                        <p style="margin:0 0 28px;font-size:15px;line-height:1.7;color:#64748b;">
                                            Bạn được mời tham gia cuộc họp với mã số
                                            <strong>#%d</strong>.
                                            Vui lòng xem thông tin chi tiết bên dưới và xác nhận tham gia.
                                        </p>
                
                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0"
                                               style="background:#f8fafc;border:1px solid #e2e8f0;border-radius:12px;padding:24px;">
                
                                            <tr>
                                                <td style="padding:8px 0;width:140px;font-size:14px;font-weight:600;color:#64748b;">
                                                    Tiêu đề
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;font-weight:600;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;font-weight:600;color:#64748b;">
                                                    Thời gian bắt đầu
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;font-weight:600;color:#64748b;">
                                                    Thời gian kết thúc
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;font-weight:600;color:#64748b;">
                                                    Phòng họp
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;font-weight:600;color:#2563eb;">
                                                    %s
                                                </td>
                                            </tr>
                
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;font-weight:600;color:#64748b;">
                                                    Địa điểm
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;color:#0f172a;">
                                                    %s
                                                </td>
                                            </tr>
                
                                            <tr>
                                                <td style="padding:8px 0;font-size:14px;font-weight:600;color:#64748b;vertical-align:top;">
                                                    Nội dung
                                                </td>
                                                <td style="padding:8px 0;font-size:15px;color:#0f172a;line-height:1.6;">
                                                    %s
                                                </td>
                                            </tr>
                
                                        </table>
                
                                        <table width="100%%" cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                                <td align="center" style="padding:35px 0 20px;">
                
                                                    <a href="%s"
                                                       target="_blank"
                                                       style="
                                                            display:inline-block;
                                                            background:#10b981;
                                                            color:#ffffff;
                                                            text-decoration:none;
                                                            padding:14px 36px;
                                                            border-radius:10px;
                                                            font-size:15px;
                                                            font-weight:600;
                                                            border:1px solid #10b981;
                                                       ">
                                                        ✓ Xác Nhận Tham Gia
                                                    </a>
                
                                                </td>
                                            </tr>
                                        </table>
                
                                        <p style="margin:0;text-align:center;font-size:13px;color:#64748b;">
                                            Nếu nút trên không hoạt động, vui lòng sử dụng liên kết bên dưới:
                                        </p>
                
                                        <p style="text-align:center;margin-top:12px;word-break:break-all;">
                                            <a href="%s"
                                               style="color:#2563eb;text-decoration:none;font-size:13px;">
                                                %s
                                            </a>
                                        </p>
                
                                        <p style="margin-top:28px;text-align:center;font-size:13px;color:#94a3b8;font-style:italic;">
                                            Vui lòng xác nhận trước thời điểm cuộc họp bắt đầu.
                                        </p>
                
                                    </td>
                                </tr>
                
                                <tr>
                                    <td style="
                                        padding:24px 30px;
                                        background:#f8fafc;
                                        border-top:1px solid #e2e8f0;
                                        text-align:center;
                                    ">
                                        <p style="margin:0;font-size:12px;color:#94a3b8;line-height:1.6;">
                                            Đây là email được gửi tự động từ hệ thống ScheduleMeeting.<br>
                                            Vui lòng không phản hồi trực tiếp email này.<br>
                                            © 2026 ScheduleMeeting Utility. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                
                            </table>
                
                        </td>
                    </tr>
                </table>
                
                </body>
                </html>
                """;
        return html.formatted(
                payload.bookingId(),
                payload.title(),
                payload.startTime(),
                payload.endTime(),
                payload.room(),
                payload.address(),
                payload.description(),
                actionUrl,
                actionUrl,
                actionUrl
        );
    }

    //gửi email cho người đăng ký
    @Override
    public void sendEmailApproveReject(ApproveRejectRecurrencePayload payload) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    "UTF-8"
            );

            helper.setTo(payload.email());
            helper.setSubject(payload.title());

            String html = """
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial, sans-serif;">
                        <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    
                            <h2>%s</h2>
                    
                            <p>%s</p>
                    
                            <hr>
                    
                            <p style="color: #666;">
                                Đây là email tự động từ hệ thống đặt lịch họp.
                            </p>
                    
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    payload.title(),
                    payload.message()
            );

            helper.setText(html, true);

            javaMailSender.send(mimeMessage);

        } catch (MessagingException e) {
            EmailErrorParser.parseException(e);
        }
    }

    //gửi email nhắc nhớ trước lịch họp cho người tham gia
    @Override
    public void sendEmailRemindingBooking(RemindingBookingPayload payload) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setSubject(StringCommon.APP_NAME_UPPER_CASE);

            helper.setTo(APP_EMAIL);

            helper.setBcc(payload.emails().toArray(new String[0]));

            helper.setText(contentReminding(payload), true);

            javaMailSender.send(message);

        } catch (Exception e) {
            EmailErrorParser.parseException(e);
        }
    }

    private String contentReminding(RemindingBookingPayload payload) {
        return """
                <!DOCTYPE html>
                <html>
                <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                    <div style="max-width: 600px; margin: auto; background: white; padding: 24px; border-radius: 8px;">
                
                        <h2 style="color: #2563eb;">
                            🔔 Nhắc lịch họp
                        </h2>
                
                        <p>
                            Cuộc họp <strong>%s</strong>
                            tại phòng <strong>%s</strong>
                            sẽ diễn ra sau <strong>%d phút</strong>.
                        </p>
                
                        <p>
                            Vui lòng chuẩn bị và tham gia đúng giờ.
                            (Nếu chưa xác nhận tham gia, hãy xác nhận trước!).
                        </p>
                
                        <hr style="margin: 24px 0;">
                
                        <p style="font-size: 12px; color: #6b7280;">
                            Email được gửi tự động từ hệ thống %s.
                        </p>
                
                    </div>
                </body>
                </html>
                """
                .formatted(
                        payload.bookingTitle(),
                        payload.roomName(),
                        payload.minutes(),
                        StringCommon.APP_NAME_UPPER_CASE
                );
    }

    //gửi email thông báo cập nhật cho người tham gia
    @Override
    public void sendEmailApproveUpdate(UpdateApprovePayload payload) {

        String subject = "[THÔNG BÁO] Lịch họp đã được cập nhật";

        String html = approveUpdateContent(payload);

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    UTF8
            );

            helper.setSubject(subject);
            helper.setTo(APP_EMAIL);
            helper.setBcc(payload.receivers().toArray(new String[0]));
            helper.setText(html, true);

            javaMailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send update meeting email", e);
        }
    }

    private String approveUpdateContent(UpdateApprovePayload payload) {
        return """
                <html>
                <body style="font-family: Arial, sans-serif">
                
                    <h2>📢 Thông báo cập nhật lịch họp</h2>
                
                    <p>Lịch họp mà bạn tham gia đã được cập nhật. Vui lòng kiểm tra thông tin mới nhất:</p>
                
                    <table border="1" cellpadding="8" cellspacing="0" style="border-collapse: collapse">
                        <tr>
                            <td><b>Tiêu đề</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Mô tả</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Địa điểm</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Phòng họp</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Bắt đầu</b></td>
                            <td>%s</td>
                        </tr>
                        <tr>
                            <td><b>Kết thúc</b></td>
                            <td>%s</td>
                        </tr>
                    </table>
                
                    <br/>
                
                    <p>
                        Vui lòng kiểm tra lại lịch trình của bạn để đảm bảo có thể tham gia cuộc họp.
                    </p>
                
                    <p>
                        Trân trọng,<br/>
                        Hệ thống quản lý lịch họp
                    </p>
                
                </body>
                </html>
                """
                .formatted(
                        payload.title(),
                        payload.description(),
                        payload.address(),
                        payload.room(),
                        payload.startTime(),
                        payload.endTime()
                );
    }

    //gửi email lịch họp bị hủy cho người tham gia (trường hợp người đặt lịch tự hủy)
    @Override
    public void sendEmailCancelBooking(CancelBookingPayload payload) {
        String subject = "[THÔNG BÁO] Lịch họp đã bị hủy";
        String html = cancelBookingContent(payload);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    UTF8
            );
            helper.setSubject(subject);
            helper.setTo(APP_EMAIL);
            helper.setBcc(payload.receivers().toArray(new String[0]));
            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send update meeting email", e);
        }
    }

    private String cancelBookingContent(CancelBookingPayload payload) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:24px;background:#f5f6fa;font-family:Arial,sans-serif;color:#333333;">
                
                    <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:700px;margin:auto;background:#ffffff;border-radius:8px;border:1px solid #e5e7eb;">
                
                        <tr>
                            <td style="background:#dc3545;color:#ffffff;padding:20px 24px;font-size:22px;font-weight:bold;border-radius:8px 8px 0 0;">
                                ❌ %s
                            </td>
                        </tr>
                
                        <tr>
                            <td style="padding:24px;">
                
                                <p style="font-size:15px;">
                                    Xin chào,
                                </p>
                
                                <p style="font-size:15px;line-height:1.7;">
                                    Lịch họp dưới đây đã được <strong>hủy</strong>.
                                </p>
                
                                <table width="100%%" cellpadding="8" cellspacing="0"
                                       style="border-collapse:collapse;margin-top:20px;border:1px solid #dddddd;">
                
                                    <tr style="background:#f8f9fa;">
                                        <td width="180"><strong>Mã đặt phòng</strong></td>
                                        <td>%d</td>
                                    </tr>
                
                                    <tr>
                                        <td><strong>Tiêu đề cuộc họp</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr style="background:#f8f9fa;">
                                        <td><strong>Phòng họp</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr>
                                        <td><strong>Địa điểm</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr style="background:#f8f9fa;">
                                        <td><strong>Thời gian bắt đầu</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr>
                                        <td><strong>Thời gian kết thúc</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr style="background:#fff3cd;">
                                        <td><strong>Lý do hủy</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                </table>
                
                                <p style="margin-top:24px;font-size:15px;">
                                    Vui lòng bỏ qua lịch họp này trong kế hoạch làm việc của bạn.
                                </p>
                
                                <p style="margin-top:32px;">
                                    Trân trọng,<br>
                                    <strong>Schedule Meeting System</strong>
                                </p>
                
                            </td>
                        </tr>
                
                        <tr>
                            <td style="padding:16px;background:#f8f9fa;color:#888888;font-size:12px;text-align:center;border-radius:0 0 8px 8px;">
                                Đây là email tự động, vui lòng không trả lời email này.
                            </td>
                        </tr>
                
                    </table>
                
                </body>
                </html>
                """.formatted(
                StringCommon.TITLE_NOTIFICATION_CANCEL_BOOKING,
                payload.bookingId(),
                payload.title(),
                payload.room(),
                payload.address(),
                payload.startTime(),
                payload.endTime(),
                payload.reason() == null || payload.reason().isBlank()
                        ? "Không có"
                        : payload.reason()
        );
    }

    //gửi email lịch họp bị hủy cho người tham gia (trường hợp phòng bảo trì)
    @Override
    public void sendEmailCancelBookingToAttendee(SimpleCancelBookingPayload payload) {
        String subject = "[THÔNG BÁO] Lịch họp đã bị hủy";
        String html = cancelBookingContent(payload);
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    true,
                    UTF8
            );
            helper.setSubject(subject);
            helper.setTo(APP_EMAIL);
            helper.setBcc(payload.receivers().toArray(new String[0]));
            helper.setText(html, true);
            javaMailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send update meeting email", e);
        }
    }

    private String cancelBookingContent(SimpleCancelBookingPayload payload) {
        return """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="margin:0;padding:24px;background:#f5f6fa;font-family:Arial,sans-serif;color:#333333;">
                
                    <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:700px;margin:auto;background:#ffffff;border-radius:8px;border:1px solid #e5e7eb;">
                
                        <tr>
                            <td style="background:#dc3545;color:#ffffff;padding:20px 24px;font-size:22px;font-weight:bold;border-radius:8px 8px 0 0;">
                                ❌ %s
                            </td>
                        </tr>
                
                        <tr>
                            <td style="padding:24px;">
                
                                <p style="font-size:15px;">
                                    Xin chào,
                                </p>
                
                                <p style="font-size:15px;line-height:1.7;">
                                    Lịch họp dưới đây đã được <strong>hủy</strong>.
                                </p>
                
                                <table width="100%%" cellpadding="8" cellspacing="0"
                                       style="border-collapse:collapse;margin-top:20px;border:1px solid #dddddd;">
                
                                    <tr style="background:#f8f9fa;">
                                        <td width="180"><strong>Mã đặt phòng</strong></td>
                                        <td>%d</td>
                                    </tr>
                
                                    <tr>
                                        <td><strong>Tiêu đề cuộc họp</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr style="background:#f8f9fa;">
                                        <td><strong>Thời gian bắt đầu</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr>
                                        <td><strong>Thời gian kết thúc</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                    <tr style="background:#fff3cd;">
                                        <td><strong>Lý do hủy</strong></td>
                                        <td>%s</td>
                                    </tr>
                
                                </table>
                
                                <p style="margin-top:24px;font-size:15px;">
                                    Vui lòng bỏ qua lịch họp này trong kế hoạch làm việc của bạn.
                                </p>
                
                                <p style="margin-top:32px;">
                                    Trân trọng,<br>
                                    <strong>Schedule Meeting System</strong>
                                </p>
                
                            </td>
                        </tr>
                
                        <tr>
                            <td style="padding:16px;background:#f8f9fa;color:#888888;font-size:12px;text-align:center;border-radius:0 0 8px 8px;">
                                Đây là email tự động, vui lòng không trả lời email này.
                            </td>
                        </tr>
                
                    </table>
                
                </body>
                </html>
                """.formatted(
                StringCommon.TITLE_NOTIFICATION_CANCEL_BOOKING,
                payload.bookingId(),
                payload.title(),
                payload.startTime(),
                payload.endTime(),
                payload.reason() == null || payload.reason().isBlank()
                        ? "Không có"
                        : payload.reason()
        );
    }
}