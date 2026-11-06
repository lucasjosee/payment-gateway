package com.lucas.gateway.charge;

import java.time.Instant;
import java.util.UUID;

// O que o ChargeService entrega para o lado de quem paga. O lojista vem só como id, para o service continuar sem
// depender do pacote merchant, e quem transforma esse id em nome é o simulador.
public record PixCharge(UUID merchantId, long amountCents, String currency, ChargeStatus status, Instant expiresAt) {
}
