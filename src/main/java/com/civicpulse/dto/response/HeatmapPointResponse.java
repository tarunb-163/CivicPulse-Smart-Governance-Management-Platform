package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapPointResponse {
    private double lat;
    private double lng;
    private double intensity;
    private String status;
    private String urgency;
}
