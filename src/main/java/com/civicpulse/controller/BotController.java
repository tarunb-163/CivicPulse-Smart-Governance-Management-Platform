package com.civicpulse.controller;

import com.civicpulse.dto.request.BotMessageRequest;
import com.civicpulse.dto.response.BotMessageResponse;
import com.civicpulse.service.BotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bot")
@RequiredArgsConstructor
@Tag(name = "CivicBot", description = "CivicBot rule-based conversational assistant")
public class BotController {

    private final BotService botService;

    @PostMapping("/message")
    @Operation(summary = "Send a message or query to CivicBot")
    public ResponseEntity<BotMessageResponse> sendMessage(@Valid @RequestBody BotMessageRequest request) {
        return ResponseEntity.ok(botService.processMessage(request));
    }
}
