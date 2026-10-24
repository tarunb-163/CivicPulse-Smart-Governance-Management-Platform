package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummaryResponse {
    private long totalComplaints;
    private long pendingComplaints;
    private long inProgressComplaints;
    private long resolvedComplaints;
    private long closedComplaints;
    private long rejectedComplaints;
    private long escalatedComplaints;
    private long slaBreachedCount;
    private double avgResolutionHours;
    private double resolutionRatePercentage;
    private long totalCitizens;
    private long totalOfficers;
    private double overallCitizenRating;
}
