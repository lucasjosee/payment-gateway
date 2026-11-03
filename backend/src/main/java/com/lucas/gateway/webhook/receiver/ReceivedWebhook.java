package com.lucas.gateway.webhook.receiver;

import java.time.Instant;

public record ReceivedWebhook(Instant receivedAt, String body) {
}
