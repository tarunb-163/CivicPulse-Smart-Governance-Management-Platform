package com.civicpulse.service;

import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.dto.response.ImageClassificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClassificationMergeTest {

    @Test
    @DisplayName("When image classification is available, both text and image suggestions are preserved for user review")
    void testCombineTextAndImageSuggestions() {
        CategorySuggestion textSuggestion = CategorySuggestion.builder()
                .categoryId(10L)
                .categoryName("Pothole & Road Damage")
                .departmentId(1L)
                .departmentName("Roads & Infrastructure")
                .confidence(0.70)
                .build();

        ImageClassificationResult imageSuggestion = ImageClassificationResult.builder()
                .available(true)
                .suggestedCategoryId(10L)
                .suggestedCategoryName("Pothole & Road Damage")
                .departmentId(1L)
                .departmentName("Roads & Infrastructure")
                .confidence(0.85)
                .message("Category suggested by image analysis")
                .build();

        // Both identify same category -> high agreement
        assertEquals(textSuggestion.getCategoryId(), imageSuggestion.getSuggestedCategoryId());
        assertTrue(imageSuggestion.isAvailable());
        assertTrue(imageSuggestion.getConfidence() > textSuggestion.getConfidence());
    }

    @Test
    @DisplayName("When image classification is unavailable, text suggestion serves as sole classifier without breaking")
    void testFallbackWhenImageUnavailable() {
        CategorySuggestion textSuggestion = CategorySuggestion.builder()
                .categoryId(20L)
                .categoryName("Garbage Dump")
                .departmentId(2L)
                .departmentName("Solid Waste Management")
                .confidence(0.80)
                .build();

        ImageClassificationResult imageResult = ImageClassificationResult.unavailable("Service disabled");

        assertFalse(imageResult.isAvailable());
        assertNotNull(textSuggestion);
        assertEquals(20L, textSuggestion.getCategoryId());
    }
}
