package com.lucas.gateway.webhook;

import java.util.List;

// Mesmo formato do ChargePage, para o front paginar as duas listas do mesmo jeito.
public record WebhookDeliveryPage(List<WebhookDeliverySummary> items, int page, int size, long totalItems,
		int totalPages) {
}
