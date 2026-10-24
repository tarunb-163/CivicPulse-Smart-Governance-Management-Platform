package com.civicpulse.service;

import com.civicpulse.enums.Urgency;
import com.civicpulse.service.impl.PriorityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PriorityServiceTest {

    private PriorityService priorityService;

    @BeforeEach
    void setUp() {
        priorityService = new PriorityServiceImpl();
    }

    @Test
    @DisplayName("Calculate priority with baseline LOW urgency, severity 1, 0 upvotes, 0 age")
    void testBaselineCalculation() {
        // urgency: LOW (1)*3 = 3.0
        // severity: 1*2 = 2.0
        // upvotes: 0*0.5 = 0.0
        // ageDays: 0*0.7 = 0.0
        // Expected: 5.0
        double score = priorityService.computePriority(Urgency.LOW, 1, 0, 0.0);
        assertEquals(5.0, score);
    }

    @Test
    @DisplayName("Calculate priority with CRITICAL urgency, severity 5, 25 upvotes (capped at 20), 10 days age")
    void testMaxClampingCalculation() {
        // urgency: CRITICAL (4)*3 = 12.0
        // severity: 5*2 = 10.0
        // upvotes: min(25, 20)*0.5 = 10.0
        // ageDays: 10*0.7 = 7.0
        // Expected: 12 + 10 + 10 + 7 = 39.0
        double score = priorityService.computePriority(Urgency.CRITICAL, 5, 25, 10.0);
        assertEquals(39.0, score);
    }

    @Test
    @DisplayName("Calculate priority with MEDIUM urgency, severity 3, 4 upvotes, 2 days age")
    void testStandardCalculation() {
        // urgency: MEDIUM (2)*3 = 6.0
        // severity: 3*2 = 6.0
        // upvotes: 4*0.5 = 2.0
        // ageDays: 2*0.7 = 1.4
        // Expected: 6 + 6 + 2 + 1.4 = 15.4
        double score = priorityService.computePriority(Urgency.MEDIUM, 3, 4, 2.0);
        assertEquals(15.4, score);
    }
}
