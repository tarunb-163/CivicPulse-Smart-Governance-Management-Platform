package com.civicpulse.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MergeComplaintRequest {

    @JsonAlias({"parentComplaintId", "parentId"})
    private Long parentId;

    private Long parentComplaintId;

    @JsonAlias({"reason", "remark"})
    private String remark;

    private String reason;

    public Long getEffectiveParentId() {
        return parentId != null ? parentId : parentComplaintId;
    }

    public String getEffectiveRemark() {
        if (remark != null && !remark.isBlank()) return remark.trim();
        if (reason != null && !reason.isBlank()) return reason.trim();
        return "Merged duplicate complaint";
    }
}
