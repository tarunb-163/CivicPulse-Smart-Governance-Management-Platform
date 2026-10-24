package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotspotPredictionResponse {
    private String categoryName;
    private String departmentName;
    private String area; // e.g. Davanagere Ward / Sector
    private double fourWeekAverageWeekly;
    private long lastWeekCount;
    private double ratio;
    private boolean spikeAlert; // true if ratio >= 1.5
    private String recommendation;

    public long getCurrentWeekCount() {
        return lastWeekCount;
    }

    public double getFourWeekAvgCount() {
        return fourWeekAverageWeekly;
    }

    public double getSpikeRatio() {
        return ratio;
    }
}
