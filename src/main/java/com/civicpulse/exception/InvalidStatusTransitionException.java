package com.civicpulse.exception;

import com.civicpulse.enums.ComplaintStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ComplaintStatus from, ComplaintStatus to) {
        super("Invalid status transition from " + from + " to " + to);
    }

    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
