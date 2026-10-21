package com.lucas.gateway.charge;

import java.time.Instant;
import java.util.UUID;

// A cobrança como aparece na listagem, sem o histórico de eventos. Trazer os eventos custaria uma consulta a mais
// para cada cobrança da página, e quem precisa do histórico abre o detalhe em GET /charges/{id}.
public record ChargeSummary(UUID id, ChargeStatus status, long amountCents, String currency, String cardLast4,
		String declineReason, Instant createdAt) {
}
