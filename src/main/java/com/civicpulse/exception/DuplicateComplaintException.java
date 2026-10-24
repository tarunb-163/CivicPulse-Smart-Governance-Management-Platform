package com.civicpulse.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class DuplicateComplaintException extends RuntimeException {
    private final String conflictType; // DUPLICATE_EXACT or DUPLICATE_SUSPECTED
    private final String existingCode;
    private final List<?> candidates;

    public DuplicateComplaintException(String message, String conflictType, String existingCode, List<?> candidates) {
        super(message);
        this.conflictType = conflictType;
        this.existingCode = existingCode;
        this.candidates = candidates;
    }

    public static DuplicateComplaintException exact(String message, String existingCode) {
        return new DuplicateComplaintException(message, "DUPLICATE_EXACT", existingCode, null);
    }

    public static DuplicateComplaintException suspected(String message, List<?> candidates) {
        return new DuplicateComplaintException(message, "DUPLICATE_SUSPECTED", null, candidates);
    }
}
