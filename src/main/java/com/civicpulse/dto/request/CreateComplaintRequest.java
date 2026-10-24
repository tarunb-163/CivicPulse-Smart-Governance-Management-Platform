package com.civicpulse.dto.request;

import com.civicpulse.enums.Urgency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    private Long categoryId;

    @Builder.Default
    private Urgency urgency = Urgency.MEDIUM;

    private String address;
    private Double latitude;
    private Double longitude;

    @Builder.Default
    private boolean anonymous = false;

    @Builder.Default
    private boolean confirmNotDuplicate = false;
}
