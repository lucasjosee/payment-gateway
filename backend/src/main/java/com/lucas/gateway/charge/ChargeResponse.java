package com.lucas.gateway.charge;

import java.time.Instant;
import java.util.UUID;

public record ChargeResponse(UUID id, ChargeStatus status, long amountCents, String currency, String cardLast4,
		String declineReason, Instant createdAt) {
}
