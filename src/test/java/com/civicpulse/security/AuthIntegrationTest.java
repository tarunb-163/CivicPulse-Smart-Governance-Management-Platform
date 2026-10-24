package com.civicpulse.security;

import com.civicpulse.dto.request.LoginRequest;
import com.civicpulse.dto.request.RegisterRequest;
import com.civicpulse.entity.User;
import com.civicpulse.enums.Role;
import com.civicpulse.repository.AuditLogRepository;
import com.civicpulse.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        userRepository.findByEmail("kavya@civicpulse.test").ifPresent(userRepository::delete);
        userRepository.findByEmail("arun@civicpulse.test").ifPresent(userRepository::delete);
    }

    @Test
    @DisplayName("Register new citizen successfully")
    void testRegisterSuccess() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .name("Kavya Sharma")
                .email("kavya@civicpulse.test")
                .password("Password@123")
                .phone("+91 9876599999")
                .acceptedTerms(true)
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("kavya@civicpulse.test"))
                .andExpect(jsonPath("$.user.role").value("CITIZEN"))
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Login with valid credentials returns JWT tokens")
    void testLoginSuccess() throws Exception {
        User user = User.builder()
                .name("Arun Kumar")
                .email("arun@civicpulse.test")
                .passwordHash(passwordEncoder.encode("Secret@123"))
                .role(Role.CITIZEN)
                .enabled(true)
                .emailVerified(true)
                .consentVersion("v1.0")
                .consentAt(java.time.Instant.now())
                .build();
        userRepository.save(user);

        LoginRequest loginReq = LoginRequest.builder()
                .email("arun@civicpulse.test")
                .password("Secret@123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("arun@civicpulse.test"));
    }

    @Test
    @DisplayName("Login with invalid password returns 401 Unauthorized")
    void testLoginFailure() throws Exception {
        User user = User.builder()
                .name("Arun Kumar")
                .email("arun@civicpulse.test")
                .passwordHash(passwordEncoder.encode("Secret@123"))
                .role(Role.CITIZEN)
                .enabled(true)
                .emailVerified(true)
                .consentVersion("v1.0")
                .build();
        userRepository.save(user);

        LoginRequest loginReq = LoginRequest.builder()
                .email("arun@civicpulse.test")
                .password("WrongPassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
