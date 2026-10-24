package com.civicpulse.service;

import com.civicpulse.dto.request.RegisterRequest;
import com.civicpulse.dto.response.AuthResponse;
import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import com.civicpulse.exception.BadRequestException;
import com.civicpulse.repository.PasswordResetTokenRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.security.JwtService;
import com.civicpulse.security.LoginAttemptService;
import com.civicpulse.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ConsentTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private EmailService emailService;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private LoginAttemptService loginAttemptService;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Registration rejects when terms & privacy consent is not accepted")
    void testRegistrationRejectsWithoutConsent() {
        RegisterRequest req = RegisterRequest.builder()
                .name("Arun Kumar")
                .email("arun@example.com")
                .password("Password@123")
                .acceptedTerms(false)
                .build();

        assertThrows(BadRequestException.class, () -> authService.register(req, "127.0.0.1"));
    }

    @Test
    @DisplayName("Registration succeeds and records consent version and timestamp when accepted")
    void testRegistrationSavesConsentMetadata() {
        RegisterRequest req = RegisterRequest.builder()
                .name("Arun Kumar")
                .email("arun@example.com")
                .password("Password@123")
                .acceptedTerms(true)
                .build();

        when(userRepository.existsByEmail("arun@example.com")).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("hashedPass");
        when(jwtService.generateAccessToken(any())).thenReturn("mock-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("mock-refresh-token");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(101L);
            return u;
        });

        AuthResponse response = authService.register(req, "127.0.0.1");
        assertNotNull(response);

        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getConsentVersion() != null &&
                user.getConsentAt() != null &&
                user.getRole() == Role.CITIZEN
        ));
    }
}
