package com.civicpulse.service;

import com.civicpulse.dto.response.CategorySuggestion;

public interface ClassifierService {
    CategorySuggestion classify(String text);
}
