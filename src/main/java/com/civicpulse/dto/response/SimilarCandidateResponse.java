package com.civicpulse.dto.response;

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
public class SimilarCandidateResponse {
    private Long id;
    private String publicCode;
    private String title;
    private double distanceMeters;
    private double similarityScore;
    private ComplaintStatus status;
    private int upvoteCount;
    private String categoryName;
    private Instant createdAt;
}
