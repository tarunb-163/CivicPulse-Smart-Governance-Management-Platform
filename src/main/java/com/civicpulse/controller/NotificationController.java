package com.civicpulse.controller;

import com.civicpulse.dto.response.NotificationResponse;
import com.civicpulse.security.UserPrincipal;
import com.civicpulse.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "User notification management")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    @Operation(summary = "Get current user notifications")
    public ResponseEntity<Page<NotificationResponse>> getNotifications(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(notificationService.getUserNotifications(principal.getId(), pageable));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "Get unread notifications count for badge")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(Map.of("unreadCount", notificationService.getUnreadCount(principal.getId())));
    }

    @RequestMapping(value = "/{id}/read", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Mark a notification as read (supports PATCH, PUT, POST)")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAsRead(id, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @RequestMapping(value = "/read-all", method = {RequestMethod.PATCH, RequestMethod.PUT, RequestMethod.POST})
    @Operation(summary = "Mark all notifications as read (supports PATCH, PUT, POST)")
    public ResponseEntity<Void> markAllAsRead(
            @AuthenticationPrincipal UserPrincipal principal) {
        notificationService.markAllAsRead(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
