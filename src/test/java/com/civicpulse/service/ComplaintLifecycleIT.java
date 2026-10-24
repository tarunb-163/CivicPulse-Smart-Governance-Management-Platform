package com.civicpulse.service;

import com.civicpulse.dto.request.*;
import com.civicpulse.dto.response.AuthResponse;
import com.civicpulse.dto.response.ComplaintResponse;
import com.civicpulse.entity.Category;
import com.civicpulse.entity.Department;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.enums.Urgency;
import com.civicpulse.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ComplaintLifecycleIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User citizen;
    private User officer;
    private Category category;
    private Department department;

    @BeforeEach
    void setUp() {
        if (departmentRepository.count() == 0) {
            department = departmentRepository.save(Department.builder()
                    .name("Roads Infrastructure")
                    .code("ROADS")
                    .active(true)
                    .build());
        } else {
            department = departmentRepository.findAll().get(0);
        }

        if (categoryRepository.count() == 0) {
            category = categoryRepository.save(Category.builder()
                    .name("Pothole & Road Damage")
                    .department(department)
                    .severityWeight(3)
                    .defaultSlaHours(48)
                    .active(true)
                    .build());
        } else {
            category = categoryRepository.findAll().get(0);
        }

        officer = userRepository.findByEmail("officer.test@civicpulse.test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Test Officer")
                        .email("officer.test@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.OFFICER)
                        .department(department)
                        .enabled(true)
                        .emailVerified(true)
                        .consentVersion("v1.0")
                        .build()));

        citizen = userRepository.findByEmail("citizen.test@civicpulse.test")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Test Citizen")
                        .email("citizen.test@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.CITIZEN)
                        .enabled(true)
                        .emailVerified(true)
                        .consentVersion("v1.0")
                        .build()));
    }

    @Test
    @DisplayName("Full End-to-End Complaint Lifecycle: Create -> Assign -> In Progress -> Resolve -> Feedback -> Reopen rules")
    void testFullComplaintLifecycle() throws Exception {
        // 1. Citizen login
        LoginRequest loginReq = LoginRequest.builder()
                .email("citizen.test@civicpulse.test")
                .password("Password@123")
                .build();

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse citizenAuth = objectMapper.readValue(loginResult.getResponse().getContentAsString(), AuthResponse.class);
        String citizenToken = "Bearer " + citizenAuth.getAccessToken();

        // 2. Submit complaint
        CreateComplaintRequest createReq = CreateComplaintRequest.builder()
                .title("Deep pothole near municipal bus stand")
                .description("Crater on the left lane causing vehicles to brake suddenly")
                .categoryId(category.getId())
                .urgency(Urgency.HIGH)
                .latitude(14.4644)
                .longitude(75.9218)
                .address("Bus Stand Road, Davanagere")
                .confirmNotDuplicate(true)
                .build();

        MockMultipartFile complaintPart = new MockMultipartFile(
                "complaint", "", "application/json",
                objectMapper.writeValueAsString(createReq).getBytes(StandardCharsets.UTF_8)
        );

        MvcResult createResult = mockMvc.perform(multipart("/api/v1/complaints")
                        .file(complaintPart)
                        .header("Authorization", citizenToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.publicCode").isNotEmpty())
                .andReturn();

        ComplaintResponse createdComplaint = objectMapper.readValue(createResult.getResponse().getContentAsString(), ComplaintResponse.class);
        Long complaintId = createdComplaint.getId();
        assertNotNull(complaintId);
        assertTrue(createdComplaint.getPublicCode().startsWith("CP-"));

        // 3. Officer login
        LoginRequest officerLogin = LoginRequest.builder()
                .email("officer.test@civicpulse.test")
                .password("Password@123")
                .build();

        MvcResult officerLoginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(officerLogin)))
                .andExpect(status().isOk())
                .andReturn();

        AuthResponse officerAuth = objectMapper.readValue(officerLoginResult.getResponse().getContentAsString(), AuthResponse.class);
        String officerToken = "Bearer " + officerAuth.getAccessToken();

        // 4. Officer changes status to IN_PROGRESS
        StatusUpdateRequest inProgressReq = StatusUpdateRequest.builder()
                .status(ComplaintStatus.IN_PROGRESS)
                .remark("Inspection team dispatched to asphalt the road")
                .build();

        mockMvc.perform(post("/api/v1/officer/complaints/" + complaintId + "/status")
                        .header("Authorization", officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inProgressReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // 5. Officer changes status to RESOLVED
        StatusUpdateRequest resolveReq = StatusUpdateRequest.builder()
                .status(ComplaintStatus.RESOLVED)
                .remark("Pothole filled with tar and rolled smooth")
                .build();

        mockMvc.perform(post("/api/v1/officer/complaints/" + complaintId + "/status")
                        .header("Authorization", officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resolveReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        // 6. Citizen submits 5-star feedback
        FeedbackRequest feedbackReq = FeedbackRequest.builder()
                .rating(5)
                .comment("Excellent work, road is clean now!")
                .build();

        mockMvc.perform(post("/api/v1/complaints/" + complaintId + "/feedback")
                        .header("Authorization", citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5));

        // 7. Duplicate feedback submission rejected
        mockMvc.perform(post("/api/v1/complaints/" + complaintId + "/feedback")
                        .header("Authorization", citizenToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(feedbackReq)))
                .andExpect(status().isConflict());

        // 8. Invalid status transition (RESOLVED -> ASSIGNED) returns 400
        StatusUpdateRequest invalidReq = StatusUpdateRequest.builder()
                .status(ComplaintStatus.ASSIGNED)
                .remark("Attempting invalid jump back to assigned")
                .build();

        mockMvc.perform(post("/api/v1/officer/complaints/" + complaintId + "/status")
                        .header("Authorization", officerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest());
    }
}
