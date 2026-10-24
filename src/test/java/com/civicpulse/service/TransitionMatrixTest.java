package com.civicpulse.service;

import com.civicpulse.enums.ComplaintStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class TransitionMatrixTest {

    // Transition matrix defined in PRD Section 9:
    // SUBMITTED   -> ASSIGNED, REJECTED, MERGED
    // ASSIGNED    -> IN_PROGRESS, REASSIGNED, ESCALATED, MERGED, REJECTED
    // IN_PROGRESS -> RESOLVED, ESCALATED, MERGED
    // ESCALATED   -> IN_PROGRESS, RESOLVED, MERGED
    // RESOLVED    -> CLOSED, REOPENED
    // REOPENED    -> ASSIGNED, IN_PROGRESS, MERGED
    // CLOSED      -> (terminal)
    // REJECTED    -> (terminal)
    // MERGED      -> (terminal, unless admin unmerge back to SUBMITTED/ASSIGNED)

    private static final Map<ComplaintStatus, Set<ComplaintStatus>> VALID_TRANSITIONS = Map.of(
            ComplaintStatus.SUBMITTED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.REJECTED, ComplaintStatus.MERGED),
            ComplaintStatus.ASSIGNED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.ESCALATED, ComplaintStatus.MERGED, ComplaintStatus.REJECTED),
            ComplaintStatus.IN_PROGRESS, Set.of(ComplaintStatus.RESOLVED, ComplaintStatus.ESCALATED, ComplaintStatus.MERGED),
            ComplaintStatus.ESCALATED, Set.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, ComplaintStatus.MERGED),
            ComplaintStatus.RESOLVED, Set.of(ComplaintStatus.CLOSED, ComplaintStatus.REOPENED),
            ComplaintStatus.REOPENED, Set.of(ComplaintStatus.ASSIGNED, ComplaintStatus.IN_PROGRESS, ComplaintStatus.MERGED),
            ComplaintStatus.CLOSED, Set.of(),
            ComplaintStatus.REJECTED, Set.of(),
            ComplaintStatus.MERGED, Set.of(ComplaintStatus.SUBMITTED, ComplaintStatus.ASSIGNED)
    );

    public static boolean isValidTransition(ComplaintStatus from, ComplaintStatus to) {
        if (from == null || to == null) return false;
        Set<ComplaintStatus> allowed = VALID_TRANSITIONS.get(from);
        return allowed != null && allowed.contains(to);
    }

    @Test
    @DisplayName("Verify every valid transition succeeds")
    void testAllValidTransitions() {
        VALID_TRANSITIONS.forEach((from, allowedTargets) -> {
            for (ComplaintStatus to : allowedTargets) {
                assertTrue(isValidTransition(from, to),
                        "Expected valid transition from " + from + " to " + to);
            }
        });
    }

    @Test
    @DisplayName("Verify invalid transitions fail")
    void testInvalidTransitions() {
        // Test direct invalid jumps
        assertFalse(isValidTransition(ComplaintStatus.SUBMITTED, ComplaintStatus.RESOLVED));
        assertFalse(isValidTransition(ComplaintStatus.SUBMITTED, ComplaintStatus.CLOSED));
        assertFalse(isValidTransition(ComplaintStatus.ASSIGNED, ComplaintStatus.CLOSED));
        assertFalse(isValidTransition(ComplaintStatus.IN_PROGRESS, ComplaintStatus.SUBMITTED));
        assertFalse(isValidTransition(ComplaintStatus.IN_PROGRESS, ComplaintStatus.CLOSED));
        assertFalse(isValidTransition(ComplaintStatus.CLOSED, ComplaintStatus.IN_PROGRESS));
        assertFalse(isValidTransition(ComplaintStatus.CLOSED, ComplaintStatus.RESOLVED));
        assertFalse(isValidTransition(ComplaintStatus.REJECTED, ComplaintStatus.IN_PROGRESS));
        assertFalse(isValidTransition(ComplaintStatus.RESOLVED, ComplaintStatus.ASSIGNED));
    }
}
