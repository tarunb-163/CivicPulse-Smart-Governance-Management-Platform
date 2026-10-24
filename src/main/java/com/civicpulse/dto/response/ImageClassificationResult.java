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
public class ImageClassificationResult {
    private boolean available;
    private Long suggestedCategoryId;
    private String suggestedCategoryName;
    private Long departmentId;
    private String departmentName;
    private double confidence;
    private List<CategorySuggestion> alternatives;
    private String message;

    public static ImageClassificationResult unavailable(String message) {
        return ImageClassificationResult.builder()
                .available(false)
                .message(message)
                .build();
    }
}
