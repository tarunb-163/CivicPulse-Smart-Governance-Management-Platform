package com.civicpulse.controller;

import com.civicpulse.entity.Category;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.Department;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Role;
import com.civicpulse.enums.Urgency;
import com.civicpulse.repository.CategoryRepository;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.DepartmentRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class PublicTrackingPrivacyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User owner;
    private User otherCitizen;
    private User officer;
    private Complaint testComplaint;

    @BeforeEach
    void setUp() {
        Department dept = departmentRepository.findByCode("RWT").orElseGet(() ->
                departmentRepository.save(Department.builder()
                        .name("Roads & Traffic Test")
                        .code("RWT")
                        .contactEmail("rwt@civicpulse.test")
                        .defaultSlaHours(48)
                        .active(true)
                        .build()));

        Category cat = categoryRepository.findByNameIgnoreCase("Pothole Test").orElseGet(() ->
                categoryRepository.save(Category.builder()
                        .name("Pothole Test")
                        .department(dept)
                        .severityWeight(3)
                        .defaultSlaHours(48)
                        .active(true)
                        .build()));

        owner = userRepository.findByEmail("alice.owner@civicpulse.test").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Alice Owner")
                        .email("alice.owner@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.CITIZEN)
                        .phone("+91 9876543210")
                        .emailVerified(true)
                        .enabled(true)
                        .build()));

        otherCitizen = userRepository.findByEmail("bob.stranger@civicpulse.test").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Bob Stranger")
                        .email("bob.stranger@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.CITIZEN)
                        .phone("+91 9876543211")
                        .emailVerified(true)
                        .enabled(true)
                        .build()));

        officer = userRepository.findByEmail("officer.tester@civicpulse.test").orElseGet(() ->
                userRepository.save(User.builder()
                        .name("Officer Tester")
                        .email("officer.tester@civicpulse.test")
                        .passwordHash(passwordEncoder.encode("Password@123"))
                        .role(Role.OFFICER)
                        .department(dept)
                        .phone("+91 9876543212")
                        .emailVerified(true)
                        .enabled(true)
                        .build()));

        String publicCode = "CP-PRIV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        testComplaint = complaintRepository.save(Complaint.builder()
                .publicCode(publicCode)
                .title("Severe pothole on 5th main road")
                .description("Deep pothole damaging vehicles and causing traffic jams")
                .citizen(owner)
                .department(dept)
                .category(cat)
                .status(ComplaintStatus.IN_PROGRESS)
                .urgency(Urgency.HIGH)
                .anonymous(false) // non-anonymous, but unauthenticated/other citizen MUST NOT see personal info
                .address("5th Main, Test Nagar")
                .latitude(12.9716)
                .longitude(77.5946)
                .priorityScore(75.0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build());
    }

    @Test
    @DisplayName("2a: Unauthenticated public tracking MUST NOT return citizen name, email, phone or userId")
    void testPublicTracking_Unauthenticated_OmitsAllPersonalInfo() throws Exception {
        mockMvc.perform(get("/api/v1/public/track/" + testComplaint.getPublicCode()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testComplaint.getPublicCode()))
                .andExpect(jsonPath("$.title").value(testComplaint.getTitle()))
                .andExpect(jsonPath("$.citizenName").value(nullValue()))
                .andExpect(jsonPath("$.citizenEmail").value(nullValue()))
                .andExpect(jsonPath("$.citizenPhone").doesNotExist())
                .andExpect(jsonPath("$.citizenId").value(nullValue()));
    }

    @Test
    @DisplayName("2a: Non-owner citizen MUST NOT see other citizen's name, email, phone or userId")
    void testPublicTracking_NonOwnerCitizen_OmitsAllPersonalInfo() throws Exception {
        String token = jwtService.generateAccessToken(otherCitizen);

        mockMvc.perform(get("/api/v1/public/track/" + testComplaint.getPublicCode())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testComplaint.getPublicCode()))
                .andExpect(jsonPath("$.citizenName").value(nullValue()))
                .andExpect(jsonPath("$.citizenEmail").value(nullValue()))
                .andExpect(jsonPath("$.citizenPhone").doesNotExist())
                .andExpect(jsonPath("$.citizenId").value(nullValue()));
    }

    @Test
    @DisplayName("2a: Complaint owner CAN see their own personal info on tracking endpoint")
    void testPublicTracking_Owner_CanSeeOwnInfo() throws Exception {
        String token = jwtService.generateAccessToken(owner);

        mockMvc.perform(get("/api/v1/public/track/" + testComplaint.getPublicCode())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testComplaint.getPublicCode()))
                .andExpect(jsonPath("$.citizenName").value("Alice Owner"))
                .andExpect(jsonPath("$.citizenEmail").value("alice.owner@civicpulse.test"))
                .andExpect(jsonPath("$.citizenId").value(owner.getId()));
    }

    @Test
    @DisplayName("2a: Staff (Officer) CAN see citizen info on non-anonymous complaints")
    void testPublicTracking_Staff_CanSeeNonAnonymousInfo() throws Exception {
        String token = jwtService.generateAccessToken(officer);

        mockMvc.perform(get("/api/v1/public/track/" + testComplaint.getPublicCode())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(testComplaint.getPublicCode()))
                .andExpect(jsonPath("$.citizenName").value("Alice Owner"))
                .andExpect(jsonPath("$.citizenEmail").value("alice.owner@civicpulse.test"))
                .andExpect(jsonPath("$.citizenId").value(owner.getId()));
    }
}
