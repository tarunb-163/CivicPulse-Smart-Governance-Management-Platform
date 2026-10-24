package com.civicpulse.service;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.ApiResponse;
import com.civicpulse.dto.response.AuthResponse;
import com.civicpulse.dto.response.UserResponse;
import com.civicpulse.security.UserPrincipal;

public interface AuthService {
    AuthResponse register(RegisterRequest request, String ipAddress);
    AuthResponse login(LoginRequest request, String ipAddress);
    AuthResponse refreshToken(RefreshTokenRequest request);
    ApiResponse forgotPassword(ForgotPasswordRequest request, String ipAddress);
    ApiResponse resetPassword(ResetPasswordRequest request, String ipAddress);
    ApiResponse verifyEmail(String token);

    UserResponse getCurrentUser(UserPrincipal principal);
    UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request);
    ApiResponse changePassword(UserPrincipal principal, ChangePasswordRequest request);
    UserResponse updateNotificationPreferences(UserPrincipal principal, NotificationPreferencesRequest request);
}
