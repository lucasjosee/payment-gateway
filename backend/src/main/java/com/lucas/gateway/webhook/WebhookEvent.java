package com.lucas.gateway.webhook;

import com.lucas.gateway.charge.ChargeResponse;

import java.time.Instant;

// O corpo que o lojista recebe. Em data vai a cobrança no mesmo formato do GET /charges/{id}, então o lojista
// lê o webhook com o mesmo código que já usa para ler a API.
public record WebhookEvent(String type, Instant createdAt, ChargeResponse data) {
}
