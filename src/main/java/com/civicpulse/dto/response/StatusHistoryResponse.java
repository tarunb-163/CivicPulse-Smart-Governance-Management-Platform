package com.civicpulse.dto.response;

import com.civicpulse.entity.StatusHistory;
import com.civicpulse.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistoryResponse {
    private Long id;
    private ComplaintStatus fromStatus;
    private ComplaintStatus toStatus;
    private String remark;
    private String changedByName;
    private Instant createdAt;

    public static StatusHistoryResponse fromEntity(StatusHistory h) {
        if (h == null) return null;
        return StatusHistoryResponse.builder()
                .id(h.getId())
                .fromStatus(h.getFromStatus())
                .toStatus(h.getToStatus())
                .remark(h.getRemark())
                .changedByName(h.getChangedByName() != null ? h.getChangedByName() : (h.getChangedBy() != null ? h.getChangedBy().getName() : "System"))
                .createdAt(h.getCreatedAt())
                .build();
    }
}
