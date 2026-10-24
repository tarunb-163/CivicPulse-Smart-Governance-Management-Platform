package com.civicpulse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassifyTextRequest {
    private String text;
    private String message;

    public String getQueryText() {
        if (text != null && !text.isBlank()) return text;
        if (message != null && !message.isBlank()) return message;
        return "";
    }
}
