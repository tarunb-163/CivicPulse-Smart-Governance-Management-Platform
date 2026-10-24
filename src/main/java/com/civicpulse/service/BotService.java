package com.civicpulse.service;

import com.civicpulse.dto.request.BotMessageRequest;
import com.civicpulse.dto.response.BotMessageResponse;

public interface BotService {
    BotMessageResponse processMessage(BotMessageRequest request);
}
