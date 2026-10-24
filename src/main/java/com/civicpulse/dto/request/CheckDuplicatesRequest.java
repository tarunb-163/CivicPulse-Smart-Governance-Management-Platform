package com.civicpulse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckDuplicatesRequest {
    private Double lat;
    private Double latitude;
    private Double lng;
    private Double longitude;
    private String text;

    public Double getEffectiveLat() {
        return lat != null ? lat : latitude;
    }

    public Double getEffectiveLng() {
        return lng != null ? lng : longitude;
    }
}
