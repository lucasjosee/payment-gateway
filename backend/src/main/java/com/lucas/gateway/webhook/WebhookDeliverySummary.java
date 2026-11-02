package com.lucas.gateway.webhook;

import java.time.Instant;
import java.util.UUID;

// A entrega como aparece na listagem, sem o payload. Ele é a cobrança inteira, com o histórico, e repeti-lo em
// cada linha da página só pesaria a resposta.
public record WebhookDeliverySummary(UUID id, UUID chargeId, String eventType, WebhookDeliveryStatus status,
		int attempts, Instant nextAttemptAt, Integer lastHttpStatus, Instant createdAt) {
}
