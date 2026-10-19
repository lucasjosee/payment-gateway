package com.lucas.gateway.charge.fraud;

// O que o antifraude sabe de uma cobrança antes de ela ser gravada. O número completo do cartão fica de fora:
// nenhuma regra precisa dele, e o que não circula também não aparece por engano num log.
public record ChargeAttempt(long amountCents, String currency, String cardLast4, String cardFingerprint) {
}
