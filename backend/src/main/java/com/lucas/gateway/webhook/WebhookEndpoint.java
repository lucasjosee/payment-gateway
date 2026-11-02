package com.lucas.gateway.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(updatable = false)
	private UUID merchantId;

	private String url;

	@Column(updatable = false)
	private Instant createdAt;

	protected WebhookEndpoint() {
	}

	public WebhookEndpoint(UUID merchantId, String url) {
		this.merchantId = merchantId;
		this.url = url;
		this.createdAt = Instant.now();
	}

	public void changeUrl(String url) {
		this.url = url;
	}

	public UUID getId() {
		return id;
	}

	public UUID getMerchantId() {
		return merchantId;
	}

	public String getUrl() {
		return url;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
