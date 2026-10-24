package com.civicpulse.dto.response;

import com.civicpulse.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private int severityWeight;
    private int defaultSlaHours;
    private String icon;
    private boolean active;

    public static CategoryResponse fromEntity(Category c) {
        if (c == null) return null;
        return CategoryResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .departmentId(c.getDepartment() != null ? c.getDepartment().getId() : null)
                .departmentName(c.getDepartment() != null ? c.getDepartment().getName() : null)
                .departmentCode(c.getDepartment() != null ? c.getDepartment().getCode() : null)
                .severityWeight(c.getSeverityWeight())
                .defaultSlaHours(c.getDefaultSlaHours())
                .icon(c.getIcon())
                .active(c.isActive())
                .build();
    }
}
