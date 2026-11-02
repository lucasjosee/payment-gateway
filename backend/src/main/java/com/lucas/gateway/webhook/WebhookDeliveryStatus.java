package com.lucas.gateway.webhook;

// Os nomes se repetem na CHECK constraint da V6__webhook_deliveries.sql.
public enum WebhookDeliveryStatus {

	PENDING,
	DELIVERED,
	FAILED
}
