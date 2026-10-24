package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaderboardItemResponse {
    private int rank;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private long totalHandled;
    private double resolutionRate;
    private double avgResolutionHours;
    private double avgRating;
    private double score; // composite performance index
}
