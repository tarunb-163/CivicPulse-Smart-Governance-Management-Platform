package com.civicpulse.dto.response;

import com.civicpulse.entity.Feedback;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackResponse {
    private Long id;
    private int rating;
    private String comment;
    private boolean satisfied;
    private Instant createdAt;

    public static FeedbackResponse fromEntity(Feedback f) {
        if (f == null) return null;
        return FeedbackResponse.builder()
                .id(f.getId())
                .rating(f.getRating())
                .comment(f.getComment())
                .satisfied(f.isSatisfied())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
