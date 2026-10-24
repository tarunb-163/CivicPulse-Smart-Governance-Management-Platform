package com.civicpulse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotMessageResponse {
    private String reply;
    private String intent;
    private List<String> quickReplies;
    private Object metadata;
}
