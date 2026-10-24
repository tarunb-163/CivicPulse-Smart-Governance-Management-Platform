package com.civicpulse.service;

import com.civicpulse.dto.response.NotificationResponse;
import com.civicpulse.entity.Notification;
import com.civicpulse.entity.User;
import com.civicpulse.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    Notification createNotification(User recipient, NotificationType type, String title, String message, String link);
    Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable);
    List<NotificationResponse> getRecentNotifications(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long notificationId, Long userId);
    void markAllAsRead(Long userId);
}
