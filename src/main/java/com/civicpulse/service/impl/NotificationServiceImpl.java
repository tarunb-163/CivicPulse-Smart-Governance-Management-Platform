package com.civicpulse.service.impl;

import com.civicpulse.dto.response.NotificationResponse;
import com.civicpulse.entity.Notification;
import com.civicpulse.entity.User;
import com.civicpulse.enums.NotificationType;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.NotificationRepository;
import com.civicpulse.service.EmailService;
import com.civicpulse.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final com.civicpulse.repository.UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public Notification createNotification(User recipient, NotificationType type, String title, String message, String link) {
        if (recipient == null) return null;
        if (recipient.getId() != null) {
            recipient = userRepository.findById(recipient.getId()).orElse(recipient);
        }

        Notification saved = null;
        if (recipient.isInAppNotifications()) {
            Notification notification = Notification.builder()
                    .user(recipient)
                    .type(type)
                    .title(title)
                    .message(message)
                    .link(link)
                    .read(false)
                    .build();
            saved = notificationRepository.save(notification);
        }

        if (recipient.isEmailNotifications() && recipient.getEmail() != null) {
            emailService.sendEmail(recipient.getEmail(), title, message, link);
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getRecentNotifications(Long userId) {
        return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }
}
