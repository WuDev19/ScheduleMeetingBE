package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.repository.NotificationRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }


}
