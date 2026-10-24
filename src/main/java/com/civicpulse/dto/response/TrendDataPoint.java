package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrendDataPoint {
    private String date; // YYYY-MM-DD
    private long submittedCount;
    private long resolvedCount;
}
