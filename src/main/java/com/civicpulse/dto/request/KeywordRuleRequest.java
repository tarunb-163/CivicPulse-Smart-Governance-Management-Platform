package com.civicpulse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordRuleRequest {

    @NotBlank(message = "Keyword is required")
    private String keyword;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Builder.Default
    private int weight = 1;
}
