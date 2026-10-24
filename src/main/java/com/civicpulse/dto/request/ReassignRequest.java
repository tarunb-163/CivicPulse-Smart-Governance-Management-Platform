package com.civicpulse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReassignRequest {
    private Long departmentId;
    private Long officerId;

    @NotBlank(message = "Reassignment reason is required")
    private String reason;
}
