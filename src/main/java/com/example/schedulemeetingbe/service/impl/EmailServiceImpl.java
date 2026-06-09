package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Room;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.entity.payload.BookingCancelledByMaintenancePayload;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.BookingRepository;
import com.example.schedulemeetingbe.repository.RoomRepository;
import com.example.schedulemeetingbe.repository.UserRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import com.example.schedulemeetingbe.service.base.IUserService;
import com.example.schedulemeetingbe.utils.EmailErrorParser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;
    private final IUserService iUserService;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
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
        User user = userRepository.findById(userId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Room room = roomRepository.findById(roomId).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
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

}
