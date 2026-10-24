package com.civicpulse.dto.response;

import com.civicpulse.entity.Attachment;
import com.civicpulse.enums.AttachmentKind;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileName;
    private String storedName;
    private String contentType;
    private long size;
    private AttachmentKind kind;
    private String url;
    private String imageDuplicateOf; // public code of matching complaint if duplicate detected
    private Instant createdAt;

    public static AttachmentResponse fromEntity(Attachment a) {
        if (a == null) return null;
        return AttachmentResponse.builder()
                .id(a.getId())
                .fileName(a.getFileName())
                .storedName(a.getStoredName())
                .contentType(a.getContentType())
                .size(a.getSize())
                .kind(a.getKind())
                .url("/api/v1/complaints/attachments/" + a.getStoredName())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
