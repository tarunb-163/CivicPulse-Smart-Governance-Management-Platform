package com.civicpulse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentRequest {

    @Size(max = 2000, message = "Comment must not exceed 2000 characters")
    private String body;

    private String comment;

    @Builder.Default
    private boolean internal = false; // staff-only note

    public String getBody() {
        if (body != null && !body.isBlank()) {
            return body;
        }
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
        if (this.body == null || this.body.isBlank()) {
            this.body = comment;
        }
    }

    public void setIsInternal(boolean isInternal) {
        this.internal = isInternal;
    }

    @jakarta.validation.constraints.AssertTrue(message = "Comment body is required")
    public boolean isHasContent() {
        return (body != null && !body.isBlank()) || (comment != null && !comment.isBlank());
    }
}


