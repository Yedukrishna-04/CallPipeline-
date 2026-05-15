package com.callpipeline.controller;

import com.callpipeline.dto.WebhookResponseDto;
import com.callpipeline.service.LiveKitWebhookService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/webhooks/livekit")
public class WebhookController {

    private final LiveKitWebhookService liveKitWebhookService;
    private final String webhookSecret;

    public WebhookController(
            LiveKitWebhookService liveKitWebhookService,
            @Value("${callpipeline.webhook.secret:}") String webhookSecret
    ) {
        this.liveKitWebhookService = liveKitWebhookService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping
    public ResponseEntity<WebhookResponseDto> receive(
            @RequestHeader(value = "X-CallPipeline-Webhook-Secret", required = false) String suppliedSecret,
            @RequestBody JsonNode payload
    ) {
        if (StringUtils.hasText(webhookSecret) && !webhookSecret.equals(suppliedSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(liveKitWebhookService.process(payload));
    }
}
