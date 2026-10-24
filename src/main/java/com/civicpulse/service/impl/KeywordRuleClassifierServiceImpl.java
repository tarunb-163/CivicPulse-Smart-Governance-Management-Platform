package com.civicpulse.service.impl;

import com.civicpulse.dto.response.CategorySuggestion;
import com.civicpulse.entity.Category;
import com.civicpulse.entity.Department;
import com.civicpulse.entity.KeywordRule;
import com.civicpulse.repository.CategoryRepository;
import com.civicpulse.repository.DepartmentRepository;
import com.civicpulse.repository.KeywordRuleRepository;
import com.civicpulse.service.ClassifierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordRuleClassifierServiceImpl implements ClassifierService {

    private final KeywordRuleRepository keywordRuleRepository;
    private final CategoryRepository categoryRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional(readOnly = true)
    public CategorySuggestion classify(String text) {
        if (text == null || text.trim().isEmpty()) {
            return fallbackGeneral(0.20);
        }

        String normalizedText = " " + text.toLowerCase().replaceAll("[^a-z0-9]", " ").replaceAll("\\s+", " ") + " ";
        List<KeywordRule> rules = keywordRuleRepository.findAllWithCategoryAndDepartment();

        if (rules.isEmpty()) {
            return fallbackGeneral(0.30);
        }

        Map<Category, Integer> categoryScores = new HashMap<>();

        for (KeywordRule rule : rules) {
            String kw = rule.getKeyword().toLowerCase().trim();
            if (kw.isEmpty()) continue;

            // Check if keyword is in normalized text (with word boundaries or substring)
            if (normalizedText.contains(" " + kw + " ") || normalizedText.contains(kw)) {
                categoryScores.merge(rule.getCategory(), rule.getWeight(), Integer::sum);
            }
        }

        if (categoryScores.isEmpty()) {
            return fallbackGeneral(0.35);
        }

        // Sort categories by score descending
        List<Map.Entry<Category, Integer>> sortedEntries = categoryScores.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .collect(Collectors.toList());

        Map.Entry<Category, Integer> topEntry = sortedEntries.get(0);
        Category topCategory = topEntry.getKey();
        int topScore = topEntry.getValue();

        // Calculate confidence: base 0.60 + score bonus up to 0.95
        double confidence = Math.min(0.95, 0.60 + (topScore * 0.05));

        // Build alternatives (up to 2)
        List<CategorySuggestion> alternatives = new ArrayList<>();
        for (int i = 1; i < Math.min(sortedEntries.size(), 3); i++) {
            Map.Entry<Category, Integer> altEntry = sortedEntries.get(i);
            Category altCategory = altEntry.getKey();
            double altConfidence = Math.min(0.85, 0.40 + (altEntry.getValue() * 0.05));
            alternatives.add(CategorySuggestion.builder()
                    .categoryId(altCategory.getId())
                    .categoryName(altCategory.getName())
                    .departmentId(altCategory.getDepartment().getId())
                    .departmentName(altCategory.getDepartment().getName())
                    .confidence(Math.round(altConfidence * 100.0) / 100.0)
                    .build());
        }

        return CategorySuggestion.builder()
                .categoryId(topCategory.getId())
                .categoryName(topCategory.getName())
                .departmentId(topCategory.getDepartment().getId())
                .departmentName(topCategory.getDepartment().getName())
                .confidence(Math.round(confidence * 100.0) / 100.0)
                .alternatives(alternatives)
                .build();
    }

    private CategorySuggestion fallbackGeneral(double confidence) {
        // Fallback to "General" category or first available active category
        Optional<Category> generalCat = categoryRepository.findByNameIgnoreCase("Miscellaneous Civic Grievance");
        if (generalCat.isEmpty()) {
            List<Category> all = categoryRepository.findByActiveTrue();
            if (!all.isEmpty()) {
                generalCat = Optional.of(all.get(0));
            }
        }

        if (generalCat.isPresent()) {
            Category cat = generalCat.get();
            return CategorySuggestion.builder()
                    .categoryId(cat.getId())
                    .categoryName(cat.getName())
                    .departmentId(cat.getDepartment().getId())
                    .departmentName(cat.getDepartment().getName())
                    .confidence(confidence)
                    .alternatives(Collections.emptyList())
                    .build();
        }

        // If no categories in DB yet
        return CategorySuggestion.builder()
                .categoryId(null)
                .categoryName("General Grievance")
                .departmentId(null)
                .departmentName("General")
                .confidence(confidence)
                .alternatives(Collections.emptyList())
                .build();
    }
}
