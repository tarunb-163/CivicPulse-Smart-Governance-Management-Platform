package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentPerformanceResponse {
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private long total;
    private long resolved;
    private long pending;
    private double resolutionRate;
    private double avgResolutionHours;
}
