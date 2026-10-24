package com.civicpulse.service;

import com.civicpulse.dto.request.CreateComplaintRequest;
import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.exception.DuplicateComplaintException;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.service.impl.ComplaintServiceImpl;
import com.civicpulse.util.ContentNormalizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class DuplicateGuardTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private ClassifierService classifierService;

    @InjectMocks
    private ComplaintServiceImpl complaintService;

    private User citizen;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(complaintService, "defaultRadiusMeters", 300.0);
        ReflectionTestUtils.setField(complaintService, "defaultSimilarityThreshold", 0.60);
        ReflectionTestUtils.setField(complaintService, "maxComplaintsPerHour", 5);
        ReflectionTestUtils.setField(complaintService, "maxComplaintsPerDay", 20);

        citizen = User.builder().id(101L).name("Rohan").email("rohan@example.com").build();
    }

    @Test
    @DisplayName("Exact duplicate submission within 24h throws DuplicateComplaintException with DUPLICATE_EXACT")
    void testExactDuplicateThrowsException() {
        CreateComplaintRequest request = CreateComplaintRequest.builder()
                .title("Broken Water Pipe on 4th Cross")
                .description("Clean drinking water is leaking onto road")
                .build();

        String hash = ContentNormalizer.computeHash(request.getTitle(), request.getDescription());
        Complaint existingComplaint = Complaint.builder()
                .id(501L)
                .publicCode("CP-2026-000501")
                .title(request.getTitle())
                .contentHash(hash)
                .build();

        when(complaintRepository.countByCitizenIdAndCreatedAtAfter(eq(101L), any())).thenReturn(0L);
        when(complaintRepository.findByCitizenIdAndContentHashAndCreatedAtAfter(eq(101L), eq(hash), any()))
                .thenReturn(List.of(existingComplaint));

        DuplicateComplaintException ex = assertThrows(DuplicateComplaintException.class, () ->
                complaintService.createComplaint(request, Collections.emptyList(), citizen, "127.0.0.1")
        );

        assertEquals("DUPLICATE_EXACT", ex.getConflictType());
        assertEquals("CP-2026-000501", ex.getExistingCode());
    }

    @Test
    @DisplayName("Similar nearby complaint throws DuplicateComplaintException with candidates unless confirmed")
    void testSimilarNearbyDuplicateCheck() {
        CreateComplaintRequest request = CreateComplaintRequest.builder()
                .title("Water pipe burst near hospital")
                .description("Drinking water leaking heavily on main road")
                .latitude(14.4644)
                .longitude(75.9218)
                .confirmNotDuplicate(false)
                .build();

        when(complaintRepository.countByCitizenIdAndCreatedAtAfter(eq(101L), any())).thenReturn(0L);
        when(complaintRepository.findByCitizenIdAndContentHashAndCreatedAtAfter(any(), any(), any()))
                .thenReturn(Collections.emptyList());

        Complaint nearbyOpen = Complaint.builder()
                .id(602L)
                .publicCode("CP-2026-000602")
                .title("Water pipe burst near hospital")
                .description("Drinking water leaking heavily on main road")
                .status(ComplaintStatus.IN_PROGRESS)
                .latitude(14.46445) // ~5 meters away
                .longitude(75.92185)
                .build();

        when(complaintRepository.findOpenComplaintsWithLocation(any()))
                .thenReturn(List.of(nearbyOpen));

        DuplicateComplaintException ex = assertThrows(DuplicateComplaintException.class, () ->
                complaintService.createComplaint(request, Collections.emptyList(), citizen, "127.0.0.1")
        );

        assertEquals("DUPLICATE_SUSPECTED", ex.getConflictType());
        assertNotNull(ex.getCandidates());
        assertFalse(ex.getCandidates().isEmpty());
    }
}
