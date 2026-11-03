package com.lucas.gateway.webhook.receiver;

import jakarta.validation.constraints.NotNull;

// Boolean, e não boolean, para um corpo sem o campo dar 400 em vez de desligar o modo sem ninguém pedir.
public record OfflineModeRequest(@NotNull Boolean offline) {
}
