package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicStatsResponse {
    private long totalGrievances;
    private long resolvedGrievances;
    private double resolutionRatePercentage;
    private double avgResolutionHours;
    private double citizenSatisfactionRating;
    private List<DepartmentPerformanceResponse> departmentStats;
}
