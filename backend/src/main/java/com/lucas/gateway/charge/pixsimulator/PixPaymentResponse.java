package com.lucas.gateway.charge.pixsimulator;

import com.lucas.gateway.charge.ChargeStatus;

import java.time.Instant;

// Qualquer pessoa com o id da cobrança lê esta resposta, então ela leva só o que aparece para quem paga um Pix.
public record PixPaymentResponse(long amountCents, String currency, String merchantName, ChargeStatus status,
		Instant expiresAt) {
}
