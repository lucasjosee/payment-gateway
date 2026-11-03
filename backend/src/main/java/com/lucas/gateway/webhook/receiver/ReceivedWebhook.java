package com.lucas.gateway.webhook.receiver;

import java.time.Instant;
import java.util.UUID;

public record ReceivedWebhook(Instant receivedAt, UUID eventId, boolean signatureValid, String body) {
}
