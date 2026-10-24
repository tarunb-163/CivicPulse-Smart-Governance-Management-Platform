package com.civicpulse.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotMessageRequest {

    private String message;
    private String query;
    private String text;

    public String getMessage() {
        if (message != null && !message.isBlank()) return message;
        if (query != null && !query.isBlank()) return query;
        return text != null ? text : "";
    }
}
