package com.civicpulse.dto.response;

import com.civicpulse.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Long actorId;
    private String actorEmail;
    private String action;
    private String entity;
    private String entityId;
    private String details;
    private String ip;
    private Instant createdAt;

    public static AuditLogResponse fromEntity(AuditLog a) {
        if (a == null) return null;
        return AuditLogResponse.builder()
                .id(a.getId())
                .actorId(a.getActor() != null ? a.getActor().getId() : null)
                .actorEmail(a.getActorEmail())
                .action(a.getAction())
                .entity(a.getEntity())
                .entityId(a.getEntityId())
                .details(a.getDetails())
                .ip(a.getIp())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
