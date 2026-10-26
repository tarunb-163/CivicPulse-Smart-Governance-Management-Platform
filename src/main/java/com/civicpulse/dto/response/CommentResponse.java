package com.civicpulse.dto.response;

import com.civicpulse.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private Long authorId;
    private String authorName;
    private String authorRole;
    private String body;
    private String comment;
    private boolean internal;
    private boolean isInternal;
    private Instant createdAt;

    public static CommentResponse fromEntity(Comment c) {
        if (c == null) return null;
        return CommentResponse.builder()
                .id(c.getId())
                .authorId(c.getAuthor() != null ? c.getAuthor().getId() : null)
                .authorName(c.getAuthor() != null ? c.getAuthor().getName() : "Anonymous")
                .authorRole(c.getAuthor() != null && c.getAuthor().getRole() != null ? c.getAuthor().getRole().name() : "CITIZEN")
                .body(c.getBody())
                .comment(c.getBody())
                .internal(c.isInternal())
                .isInternal(c.isInternal())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
