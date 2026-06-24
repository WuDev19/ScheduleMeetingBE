package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.User;

public interface INotificationService {
    Notification save(String title, String message, User user);
}
