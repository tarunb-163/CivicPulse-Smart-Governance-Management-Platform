package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategorySuggestion {
    private Long categoryId;
    private String categoryName;
    private Long departmentId;
    private String departmentName;
    private double confidence;
    private List<CategorySuggestion> alternatives;
}
