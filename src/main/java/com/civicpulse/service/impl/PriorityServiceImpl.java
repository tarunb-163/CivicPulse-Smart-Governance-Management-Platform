package com.civicpulse.service.impl;

import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.Urgency;
import com.civicpulse.service.PriorityService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class PriorityServiceImpl implements PriorityService {

    @Override
    public double computePriority(Urgency urgency, int severityWeight, int upvotes, double ageDays) {
        int urgencyWeight = switch (urgency != null ? urgency : Urgency.MEDIUM) {
            case LOW -> 1;
            case MEDIUM -> 2;
            case HIGH -> 3;
            case CRITICAL -> 4;
        };

        int clampedSeverity = Math.max(1, Math.min(5, severityWeight));
        int clampedUpvotes = Math.min(Math.max(0, upvotes), 20);
        double clampedAge = Math.max(0.0, ageDays);

        // Formula: urgency*3 + severityWeight*2 + min(upvotes,20)*0.5 + ageDays*0.7
        double score = (urgencyWeight * 3.0) + (clampedSeverity * 2.0) + (clampedUpvotes * 0.5) + (clampedAge * 0.7);
        return Math.round(score * 100.0) / 100.0;
    }

    @Override
    public double computePriority(Complaint complaint) {
        if (complaint == null) return 0.0;

        Urgency urgency = complaint.getUrgency();
        int severity = complaint.getCategory() != null ? complaint.getCategory().getSeverityWeight() : 3;
        int upvotes = complaint.getUpvoteCount();

        double ageDays = 0.0;
        if (complaint.getCreatedAt() != null) {
            long hours = Duration.between(complaint.getCreatedAt(), Instant.now()).toHours();
            ageDays = hours / 24.0;
        }

        return computePriority(urgency, severity, upvotes, ageDays);
    }
}
