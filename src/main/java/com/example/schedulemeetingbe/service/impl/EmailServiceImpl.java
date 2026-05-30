package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.service.base.IEmailService;
import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;

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

}
