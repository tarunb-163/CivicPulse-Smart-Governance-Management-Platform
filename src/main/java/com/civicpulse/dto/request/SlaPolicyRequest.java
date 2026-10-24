package com.civicpulse.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaPolicyRequest {

    @NotNull(message = "Default SLA hours is required")
    @Min(value = 1, message = "SLA must be at least 1 hour")
    private Integer defaultSlaHours;

    @Min(1) @Max(5)
    private Integer severityWeight;
}
