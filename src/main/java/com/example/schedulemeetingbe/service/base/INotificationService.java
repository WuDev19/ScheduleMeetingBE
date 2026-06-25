package com.example.schedulemeetingbe.service.base;

import com.example.schedulemeetingbe.dto.request.notification.CreateNotificationRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.notification.NotificationResponse;
import com.example.schedulemeetingbe.dto.response.notification.UnreadCountResponse;
import com.example.schedulemeetingbe.entity.Booking;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface INotificationService {

    Map<String, Object> createNotification(CreateNotificationRequest request);

    PageResponse<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    UnreadCountResponse countUnread(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    void deleteAllNotificationSelected(List<Long> notificationId, Long userId);

    Notification save(String title, String message, User user, Booking booking);

    List<Notification> save(List<Notification> notifications);

    Optional<Notification> getNotification(Long notificationId);
}
