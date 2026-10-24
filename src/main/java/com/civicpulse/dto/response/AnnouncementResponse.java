package com.civicpulse.dto.response;

import com.civicpulse.entity.Announcement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponse {
    private Long id;
    private String title;
    private String body;
    private boolean active;
    private Instant startsAt;
    private Instant endsAt;
    private Instant createdAt;

    public static AnnouncementResponse fromEntity(Announcement a) {
        if (a == null) return null;
        return AnnouncementResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .body(a.getBody())
                .active(a.isActive())
                .startsAt(a.getStartsAt())
                .endsAt(a.getEndsAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
