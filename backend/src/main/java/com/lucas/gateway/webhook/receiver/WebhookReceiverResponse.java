package com.lucas.gateway.webhook.receiver;

import java.util.List;

// O que chegou vem do mais recente para o mais antigo.
public record WebhookReceiverResponse(String url, boolean offline, List<ReceivedWebhook> received) {
}
