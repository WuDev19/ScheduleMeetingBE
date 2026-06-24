package com.example.schedulemeetingbe.service.impl;

import com.example.schedulemeetingbe.constant.StringCommon;
import com.example.schedulemeetingbe.dto.common.CRUDResponseHelper;
import com.example.schedulemeetingbe.dto.request.notification.CreateNotificationRequest;
import com.example.schedulemeetingbe.dto.response.PageResponse;
import com.example.schedulemeetingbe.dto.response.notification.NotificationResponse;
import com.example.schedulemeetingbe.dto.response.notification.UnreadCountResponse;
import com.example.schedulemeetingbe.entity.Notification;
import com.example.schedulemeetingbe.entity.User;
import com.example.schedulemeetingbe.exception.ErrorResponse;
import com.example.schedulemeetingbe.exception.custom_exception.BusinessException;
import com.example.schedulemeetingbe.mapper.NotificationMapper;
import com.example.schedulemeetingbe.repository.NotificationRepository;
import com.example.schedulemeetingbe.service.base.INotificationService;
import com.example.schedulemeetingbe.service.base.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final IUserService iUserService;

    @Override
    public Map<String, Object> createNotification(CreateNotificationRequest request) {
        Set<User> users = iUserService.getUserUserIdIn(request.userIds());
        if (request.departmentId() != null) {
            users.addAll(iUserService.getUserInDepartment(request.departmentId()));
        }
        List<Notification> notifications = new ArrayList<>();
        users.forEach(user -> {
            Notification notification = Notification.builder()
                    .user(user)
                    .title(request.title())
                    .message(request.message())
                    .build();
            notifications.add(notification);
        });
        notificationRepository.saveAll(notifications);
        return CRUDResponseHelper.createSuccess();
    }

    @Override
    public PageResponse<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        Page<Notification> page = notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        List<NotificationResponse> result = page.getContent()
                .stream()
                .map(notification -> NotificationMapper.mapToNotificationResponse(notification, userId))
                .toList();
        return new PageResponse<>(
                page.getNumber(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                result
        );
    }

    @Transactional
    @Override
    public NotificationResponse markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new BusinessException(ErrorResponse.FAKE_AUTH_ERROR);
        }
        notification.setIsRead(true);
        return NotificationMapper.mapToNotificationResponse(notification, userId);
    }

    @Transactional
    @Override
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountResponse countUnread(Long userId) {
        long count = notificationRepository.countByUser_UserIdAndIsRead(userId, false);
        return new UnreadCountResponse(count);
    }

    @Override
    public void deleteNotification(Long notificationId, Long userId) {
        int deleted = notificationRepository.deleteByIdAndUserId(notificationId, userId);
        if (deleted == 0) {
            throw new BusinessException(ErrorResponse.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void deleteAllNotificationSelected(List<Long> notificationId, Long userId) {
        notificationRepository.deleteByNotificationIdInAndUser_UserId(notificationId, userId);
    }

    @Override
    public Notification save(String title, String message, User user) {
        Notification notification = Notification.builder()
                .title(StringCommon.TITLE_NOTIFICATION)
                .message(message)
                .user(user)
                .build();
        return notificationRepository.save(notification);
    }

    @Override
    public Optional<Notification> getNotification(Long notificationId) {
        return notificationRepository.findById(notificationId);

    }

}
