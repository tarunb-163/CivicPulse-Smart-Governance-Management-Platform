package com.civicpulse.dto.response;

import com.civicpulse.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String contactEmail;
    private int defaultSlaHours;
    private boolean active;
    private String headUserName;
    private Long headUserId;

    public static DepartmentResponse fromEntity(Department d) {
        if (d == null) return null;
        return DepartmentResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .code(d.getCode())
                .contactEmail(d.getContactEmail())
                .defaultSlaHours(d.getDefaultSlaHours())
                .active(d.isActive())
                .headUserName(d.getHeadUser() != null ? d.getHeadUser().getName() : null)
                .headUserId(d.getHeadUser() != null ? d.getHeadUser().getId() : null)
                .build();
    }
}
