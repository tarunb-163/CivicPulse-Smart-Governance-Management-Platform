package com.civicpulse.service;

import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.Urgency;

public interface PriorityService {
    double computePriority(Urgency urgency, int severityWeight, int upvotes, double ageDays);
    double computePriority(Complaint complaint);
}
