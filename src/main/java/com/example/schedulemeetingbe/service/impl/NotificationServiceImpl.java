package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.repository.NotificationRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(String title, String message, User user) {
        Notification notification = Notification.builder()
                .title(StringCommon.TITLE_NOTIFICATION)
                .message(message)
                .user(user)
                .build();
        return notificationRepository.save(notification);
    }


}
