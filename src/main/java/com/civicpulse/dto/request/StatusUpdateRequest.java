package com.civicpulse.dto.request;

import com.civicpulse.enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateRequest {

    @NotNull(message = "New status is required")
    private ComplaintStatus status;

    @NotBlank(message = "Remark is required for all status changes")
    @Size(max = 1000, message = "Remark must not exceed 1000 characters")
    private String remark;
}
