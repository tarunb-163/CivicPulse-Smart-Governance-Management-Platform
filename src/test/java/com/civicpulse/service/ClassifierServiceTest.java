package com.civicpulse.service;

import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.entity.Category;
import com.civicpulse.entity.Department;
import com.civicpulse.entity.KeywordRule;
import com.civicpulse.repository.CategoryRepository;
import com.civicpulse.repository.DepartmentRepository;
import com.civicpulse.repository.KeywordRuleRepository;
import com.civicpulse.service.impl.KeywordRuleClassifierServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ClassifierServiceTest {

    @Mock
    private KeywordRuleRepository keywordRuleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private KeywordRuleClassifierServiceImpl classifierService;

    private Category potholeCategory;
    private Category garbageCategory;
    private Category fallbackCategory;
    private Department roadsDept;
    private Department swmDept;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        roadsDept = Department.builder().id(1L).name("Roads & Infrastructure").code("ROADS").build();
        swmDept = Department.builder().id(2L).name("Solid Waste Management").code("SWM").build();

        potholeCategory = Category.builder().id(10L).name("Pothole & Road Damage").department(roadsDept).severityWeight(3).build();
        garbageCategory = Category.builder().id(20L).name("Garbage Dump").department(swmDept).severityWeight(3).build();
        fallbackCategory = Category.builder().id(99L).name("General / Other").department(roadsDept).severityWeight(2).build();

        List<KeywordRule> mockRules = List.of(
                KeywordRule.builder().id(1L).category(potholeCategory).keyword("pothole").weight(10).build(),
                KeywordRule.builder().id(2L).category(potholeCategory).keyword("crater").weight(8).build(),
                KeywordRule.builder().id(3L).category(garbageCategory).keyword("garbage").weight(10).build(),
                KeywordRule.builder().id(4L).category(garbageCategory).keyword("kachra").weight(10).build()
        );

        when(keywordRuleRepository.findAllWithCategoryAndDepartment()).thenReturn(mockRules);
        when(categoryRepository.findByNameIgnoreCase("Miscellaneous Civic Grievance")).thenReturn(Optional.of(fallbackCategory));
        when(categoryRepository.findAll()).thenReturn(List.of(potholeCategory, garbageCategory, fallbackCategory));
    }

    @Test
    @DisplayName("Classifies text with keyword match to Pothole category")
    void testPotholeClassification() {
        CategorySuggestion suggestion = classifierService.classify("There is a massive pothole on the main street");
        assertNotNull(suggestion);
        assertEquals(10L, suggestion.getCategoryId());
        assertEquals("Pothole & Road Damage", suggestion.getCategoryName());
        assertEquals("Roads & Infrastructure", suggestion.getDepartmentName());
        assertTrue(suggestion.getConfidence() > 0.5);
    }

    @Test
    @DisplayName("Classifies text with garbage keyword to Garbage category")
    void testGarbageClassification() {
        CategorySuggestion suggestion = classifierService.classify("Huge kachra and garbage dump near the market");
        assertNotNull(suggestion);
        assertEquals(20L, suggestion.getCategoryId());
        assertEquals("Garbage Dump", suggestion.getCategoryName());
        assertEquals("Solid Waste Management", suggestion.getDepartmentName());
    }

    @Test
    @DisplayName("Falls back to General category when no keyword matches")
    void testFallbackClassification() {
        CategorySuggestion suggestion = classifierService.classify("Something completely unrelated and obscure happened here");
        assertNotNull(suggestion);
        assertEquals("General / Other", suggestion.getCategoryName());
        assertTrue(suggestion.getConfidence() <= 0.40);
    }
}
