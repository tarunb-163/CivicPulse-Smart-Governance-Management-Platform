package com.civicpulse.dto.response;

import com.civicpulse.entity.DevOutbox;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DevOutboxResponse {
    private Long id;
    private String recipientEmail;
    private String subject;
    private String bodyText;
    private String link;
    private Instant createdAt;
    private Instant sentAt;

    public static DevOutboxResponse fromEntity(DevOutbox d) {
        if (d == null) return null;
        return DevOutboxResponse.builder()
                .id(d.getId())
                .recipientEmail(d.getRecipientEmail())
                .subject(d.getSubject())
                .bodyText(d.getBodyText())
                .link(d.getLink())
                .createdAt(d.getCreatedAt())
                .sentAt(d.getCreatedAt())
                .build();
    }
}
