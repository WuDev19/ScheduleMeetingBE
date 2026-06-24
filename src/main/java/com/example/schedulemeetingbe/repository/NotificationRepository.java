package com.example.schedulemeetingbe.repository;

import com.example.schedulemeetingbe.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    long countByUser_UserIdAndIsRead(Long userId, Boolean isRead);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.user.userId = :userId AND n.notificationId = :notificationId")
    int deleteByIdAndUserId(@Param("notificationId") Long notificationId, @Param("userId") Long userId);

    void deleteByNotificationIdInAndUser_UserId(List<Long> notificationIds, Long userId);
}
