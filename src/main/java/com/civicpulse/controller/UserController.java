package com.civicpulse.controller;

import com.civicpulse.dto.request.ChangePasswordRequest;
import com.civicpulse.dto.request.NotificationPreferencesRequest;
import com.civicpulse.dto.request.UpdateProfileRequest;
import com.civicpulse.dto.response.ApiResponse;
import com.civicpulse.dto.response.UserResponse;
import com.civicpulse.security.UserPrincipal;
import com.civicpulse.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Endpoints for viewing and updating the authenticated user's profile")
public class UserController {

    private final AuthService authService;

    @GetMapping({"/me", "/profile"})
    @Operation(summary = "Get current authenticated user profile")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal));
    }

    @PutMapping({"/me", "/profile"})
    @Operation(summary = "Update current user profile details")
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                                 @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(principal, request));
    }

    @PutMapping("/me/password")
    @Operation(summary = "Change current user password")
    public ResponseEntity<ApiResponse> changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                                       @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(authService.changePassword(principal, request));
    }

    @PutMapping("/me/preferences")
    @Operation(summary = "Update notification preferences")
    public ResponseEntity<UserResponse> updatePreferences(@AuthenticationPrincipal UserPrincipal principal,
                                                          @RequestBody NotificationPreferencesRequest request) {
        return ResponseEntity.ok(authService.updateNotificationPreferences(principal, request));
    }
}
