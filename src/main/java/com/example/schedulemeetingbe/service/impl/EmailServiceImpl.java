package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.repository.UserRepository;
import com.example.schedulemeetingbe.service.base.IEmailService;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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

    @Transactional
    @Override
    public void sendEmailResetPassword(String email) {
        User user = userRepository.findByEmailAndIsActiveIsTrue(email).orElseThrow(() ->
                new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int length = 10;
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        String newPassword = sb.toString() + random.nextInt(1000);
        user.setPasswordHash(bCryptPasswordEncoder.encode(newPassword));
        user.setPasswordChangedAt(ZonedDateTime.now());
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(email);
        simpleMailMessage.setSubject(StringCommon.APP_NAME_UPPER_CASE);
        simpleMailMessage.setText(
                "Mật khẩu mới của quý khách là:\n"
                        + newPassword + "\n"
                        + "Vui lòng đăng nhập sau đó đổi mật khẩu ngay."
        );
        javaMailSender.send(simpleMailMessage);
    }

}
