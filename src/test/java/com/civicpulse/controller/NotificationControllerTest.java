package com.civicpulse.controller;

import com.civicpulse.entity.Notification;
import com.civicpulse.entity.User;
import com.civicpulse.enums.NotificationType;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.NotificationRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizen;
    private String token;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        citizen = userRepository.findByEmail("notif.tester@civicpulse.test").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Notification Tester")
                        .email("notif.tester@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.CITIZEN)
                        .phone("+91 9123456789")
                        .emailVerified(true)
                        .consentVersion("1.0")
                        .consentAt(Instant.now())
                        .build())
        );
        token = jwtService.generateAccessToken(citizen);
    }

    @Test
    @DisplayName("Mark as read supports PATCH, PUT, and POST methods and marks notification read")
    void testMarkAsReadMethods() throws Exception {
        Notification n1 = notificationRepository.save(Notification.builder()
                .user(citizen)
                .title("Alert 1")
                .message("Message 1")
                .type(NotificationType.STATUS_CHANGED)
                .read(false)
                .build());

        Notification n2 = notificationRepository.save(Notification.builder()
                .user(citizen)
                .title("Alert 2")
                .message("Message 2")
                .type(NotificationType.STATUS_CHANGED)
                .read(false)
                .build());

        Notification n3 = notificationRepository.save(Notification.builder()
                .user(citizen)
                .title("Alert 3")
                .message("Message 3")
                .type(NotificationType.STATUS_CHANGED)
                .read(false)
                .build());

        // Test POST (used by JS frontend)
        mockMvc.perform(post("/api/v1/notifications/" + n1.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertTrue(notificationRepository.findById(n1.getId()).orElseThrow().isRead());

        // Test PATCH (specified by PRD)
        mockMvc.perform(patch("/api/v1/notifications/" + n2.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertTrue(notificationRepository.findById(n2.getId()).orElseThrow().isRead());

        // Test PUT
        mockMvc.perform(put("/api/v1/notifications/" + n3.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        assertTrue(notificationRepository.findById(n3.getId()).orElseThrow().isRead());
    }

    @Test
    @DisplayName("Unsupported HTTP method returns 405 Method Not Allowed")
    void testUnsupportedMethodReturns405() throws Exception {
        Notification n = notificationRepository.save(Notification.builder()
                .user(citizen)
                .title("Alert")
                .message("Message")
                .type(NotificationType.STATUS_CHANGED)
                .read(false)
                .build());

        mockMvc.perform(delete("/api/v1/notifications/" + n.getId() + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.error").value("Method Not Allowed"));
    }
}
