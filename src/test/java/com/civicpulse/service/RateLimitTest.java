package com.civicpulse.service;

import com.civicpulse.dto.request.CreateComplaintRequest;
import com.civicpulse.entity.User;
import com.civicpulse.exception.RateLimitExceededException;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.service.impl.ComplaintServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class RateLimitTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private User citizen;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(complaintService, "maxComplaintsPerHour", 5);
        ReflectionTestUtils.setField(complaintService, "maxComplaintsPerDay", 20);

        citizen = User.builder().id(101L).name("Rohan").email("rohan@example.com").build();
    }

    @Test
    @DisplayName("Exceeding 5 complaints per hour throws RateLimitExceededException with retryAfter")
    void testHourlyRateLimitExceeded() {
        CreateComplaintRequest request = CreateComplaintRequest.builder()
                .title("Street light fused on main road")
                .description("Light is out since yesterday")
                .build();

        // Simulate 5 complaints already submitted in the last hour
        when(complaintRepository.countByCitizenIdAndCreatedAtAfter(eq(101L), any())).thenReturn(5L);

        RateLimitExceededException ex = assertThrows(RateLimitExceededException.class, () ->
                complaintService.createComplaint(request, Collections.emptyList(), citizen, "127.0.0.1")
        );

        assertTrue(ex.getMessage().contains("Hourly complaint submission limit exceeded"));
        assertTrue(ex.getRetryAfterSeconds() > 0);
    }
}
