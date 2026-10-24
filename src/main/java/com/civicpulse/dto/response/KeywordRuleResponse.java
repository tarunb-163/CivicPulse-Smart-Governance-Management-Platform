package com.civicpulse.dto.response;

import com.civicpulse.entity.KeywordRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeywordRuleResponse {
    private Long id;
    private String keyword;
    private Long categoryId;
    private String categoryName;
    private Long departmentId;
    private String departmentName;
    private int weight;

    public static KeywordRuleResponse fromEntity(KeywordRule r) {
        if (r == null) return null;
        return KeywordRuleResponse.builder()
                .id(r.getId())
                .keyword(r.getKeyword())
                .categoryId(r.getCategory() != null ? r.getCategory().getId() : null)
                .categoryName(r.getCategory() != null ? r.getCategory().getName() : null)
                .departmentId(r.getCategory() != null && r.getCategory().getDepartment() != null ? r.getCategory().getDepartment().getId() : null)
                .departmentName(r.getCategory() != null && r.getCategory().getDepartment() != null ? r.getCategory().getDepartment().getName() : null)
                .weight(r.getWeight())
                .build();
    }
}
