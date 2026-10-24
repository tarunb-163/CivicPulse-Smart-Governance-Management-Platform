package com.civicpulse.dto.response;

import com.civicpulse.entity.Complaint;
import com.civicpulse.enums.ComplaintStatus;
import com.civicpulse.enums.Urgency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponse {
    private Long id;
    private String publicCode;
    private String code;
    private String title;
    private String description;
    private CategoryResponse category;
    private DepartmentResponse department;

    // Complainant info (masked if anonymous unless caller is owner or admin)
    private Long citizenId;
    private String citizenName;
    private String citizenEmail;

    private UserResponse assignedOfficer;
    private String assignedOfficerName;
    private ComplaintStatus status;
    private double priorityScore;
    private Urgency urgency;
    private boolean anonymous;

    private String address;
    private Double latitude;
    private Double longitude;

    private int upvoteCount;
    private boolean hasUpvoted;
    private boolean isFollowing;

    private Long duplicateOfId;
    private String duplicateOfCode;
    private String imageSuggestion;

    private Instant slaDueAt;
    private boolean slaOverdue;
    private Double slaRemainingHours;

    private Instant resolvedAt;
    private Instant closedAt;
    private int escalationLevel;

    private Instant createdAt;
    private Instant updatedAt;

    private List<AttachmentResponse> attachments;
    private List<StatusHistoryResponse> statusHistories;
    private List<CommentResponse> comments;
    private FeedbackResponse feedback;

    public static ComplaintResponse fromEntity(Complaint c, Long currentUserId, boolean isPrivileged) {
        if (c == null) return null;

        boolean isOwner = currentUserId != null && c.getCitizen() != null && currentUserId.equals(c.getCitizen().getId());
        boolean canSeeIdentity = !c.isAnonymous() || isOwner || isPrivileged;

        boolean isOverdue = false;
        Double remainingHours = null;
        if (c.getSlaDueAt() != null) {
            Instant now = Instant.now();
            isOverdue = c.getSlaDueAt().isBefore(now) &&
                    c.getStatus() != ComplaintStatus.RESOLVED &&
                    c.getStatus() != ComplaintStatus.CLOSED &&
                    c.getStatus() != ComplaintStatus.REJECTED &&
                    c.getStatus() != ComplaintStatus.MERGED;
            long mins = Duration.between(now, c.getSlaDueAt()).toMinutes();
            remainingHours = Math.round((mins / 60.0) * 10.0) / 10.0;
        }

        return ComplaintResponse.builder()
                .id(c.getId())
                .publicCode(c.getPublicCode())
                .code(c.getPublicCode())
                .title(c.getTitle())
                .description(c.getDescription())
                .category(CategoryResponse.fromEntity(c.getCategory()))
                .department(DepartmentResponse.fromEntity(c.getDepartment()))
                .citizenId(canSeeIdentity && c.getCitizen() != null ? c.getCitizen().getId() : null)
                .citizenName(canSeeIdentity && c.getCitizen() != null ? c.getCitizen().getName() : "Anonymous Citizen")
                .citizenEmail(canSeeIdentity && c.getCitizen() != null ? c.getCitizen().getEmail() : null)
                .assignedOfficer(UserResponse.fromEntity(c.getAssignedOfficer()))
                .assignedOfficerName(c.getAssignedOfficer() != null ? c.getAssignedOfficer().getName() : null)
                .status(c.getStatus())
                .priorityScore(c.getPriorityScore())
                .urgency(c.getUrgency())
                .anonymous(c.isAnonymous())
                .address(c.getAddress())
                .latitude(c.getLatitude())
                .longitude(c.getLongitude())
                .upvoteCount(c.getUpvoteCount())
                .duplicateOfId(c.getDuplicateOf() != null ? c.getDuplicateOf().getId() : null)
                .duplicateOfCode(c.getDuplicateOf() != null ? c.getDuplicateOf().getPublicCode() : null)
                .imageSuggestion(c.getImageSuggestion())
                .slaDueAt(c.getSlaDueAt())
                .slaOverdue(isOverdue)
                .slaRemainingHours(remainingHours)
                .resolvedAt(c.getResolvedAt())
                .closedAt(c.getClosedAt())
                .escalationLevel(c.getEscalationLevel())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
