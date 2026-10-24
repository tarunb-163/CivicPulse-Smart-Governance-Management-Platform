package com.civicpulse.service.impl;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.ApiResponse;
import com.civicpulse.dto.response.AuthResponse;
import com.civicpulse.dto.response.UserResponse;
import com.civicpulse.entity.PasswordResetToken;
import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.exception.DuplicateResourceException;
import com.civicpulse.exception.ResourceNotFoundException;
import com.civicpulse.repository.PasswordResetTokenRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.security.JwtService;
import com.civicpulse.security.LoginAttemptService;
import com.civicpulse.security.UserPrincipal;
import com.civicpulse.service.AuditLogService;
import com.civicpulse.service.AuthService;
import com.civicpulse.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final LoginAttemptService loginAttemptService;

    @Value("${app.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress) {
        if (request.getAcceptedTerms() == null || !request.getAcceptedTerms()) {
            throw new BadRequestException("You must accept the Terms of Service and Privacy Policy to register");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with email " + email + " already exists");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(email)
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(Role.CITIZEN)
                .enabled(true)
                .emailVerified(false)
                .locale("en")
                .consentVersion("1.0")
                .consentAt(Instant.now())
                .emailNotifications(true)
                .inAppNotifications(true)
                .build();

        user = userRepository.save(user);

        // Verification token
        String verifyToken = UUID.randomUUID().toString();
        PasswordResetToken verification = PasswordResetToken.builder()
                .user(user)
                .token(verifyToken)
                .expiresAt(Instant.now().plus(24, ChronoUnit.HOURS))
                .used(false)
                .build();
        passwordResetTokenRepository.save(verification);

        emailService.sendRegistrationVerification(user.getEmail(), user.getName(), verifyToken);
        auditLogService.log(user, user.getEmail(), "REGISTER", "User", user.getId().toString(), "Citizen registered", ipAddress);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtExpirationMs)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        String email = request.getEmail().trim().toLowerCase();

        // Check if temporarily locked out from repeated failed attempts
        loginAttemptService.checkIfBlocked(email);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );

            loginAttemptService.loginSucceeded(email);

            UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
            User user = userRepository.findById(principal.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            if (!user.isEnabled()) {
                throw new BadRequestException("Account is deactivated. Please contact support.");
            }

            auditLogService.log(user, user.getEmail(), "LOGIN_SUCCESS", "User", user.getId().toString(), "User logged in", ipAddress);

            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresInMs(jwtExpirationMs)
                    .user(UserResponse.fromEntity(user))
                    .build();

        } catch (BadCredentialsException e) {
            loginAttemptService.loginFailed(email);
            auditLogService.log(null, email, "LOGIN_FAILURE", "User", null, "Failed login attempt: bad credentials", ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        String email = jwtService.extractUsername(refreshToken);
        if (email == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new BadRequestException("Account is deactivated");
        }

        if (jwtService.isTokenExpired(refreshToken)) {
            throw new BadRequestException("Refresh token has expired. Please login again.");
        }

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresInMs(jwtExpirationMs)
                .user(UserResponse.fromEntity(user))
                .build();
    }

    @Override
    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request, String ipAddress) {
        String email = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .user(user)
                    .token(token)
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .used(false)
                    .build();
            passwordResetTokenRepository.save(resetToken);

            emailService.sendPasswordReset(user.getEmail(), user.getName(), token);
            auditLogService.log(user, user.getEmail(), "PASSWORD_RESET_REQUEST", "User", user.getId().toString(), "Password reset requested", ipAddress);
        }

        return ApiResponse.ok("If an account exists with that email, instructions have been sent to reset your password.");
    }

    @Override
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request, String ipAddress) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token has expired or already been used");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        auditLogService.log(user, user.getEmail(), "PASSWORD_RESET_SUCCESS", "User", user.getId().toString(), "Password successfully reset", ipAddress);

        return ApiResponse.ok("Password has been reset successfully. You can now login with your new password.");
    }

    @Override
    @Transactional
    public ApiResponse verifyEmail(String token) {
        PasswordResetToken verification = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid email verification token"));

        if (verification.isUsed() || verification.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("Verification link has expired or has already been used");
        }

        User user = verification.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verification.setUsed(true);
        passwordResetTokenRepository.save(verification);

        return ApiResponse.ok("Your email has been verified successfully! You can now log in.");
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getLocale() != null && !request.getLocale().isBlank()) {
            user.setLocale(request.getLocale().trim());
        }

        user = userRepository.save(user);
        auditLogService.log(user, user.getEmail(), "PROFILE_UPDATE", "User", user.getId().toString(), "Profile details updated", null);
        return UserResponse.fromEntity(user);
    }

    @Override
    @Transactional
    public ApiResponse changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password does not match");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        auditLogService.log(user, user.getEmail(), "PASSWORD_CHANGE", "User", user.getId().toString(), "Password changed from profile", null);
        return ApiResponse.ok("Password updated successfully");
    }

    @Override
    @Transactional
    public UserResponse updateNotificationPreferences(UserPrincipal principal, NotificationPreferencesRequest request) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (request.getEmailNotifications() != null) {
            user.setEmailNotifications(request.getEmailNotifications());
        }
        if (request.getInAppNotifications() != null) {
            user.setInAppNotifications(request.getInAppNotifications());
        }

        user = userRepository.save(user);
        return UserResponse.fromEntity(user);
    }
}
