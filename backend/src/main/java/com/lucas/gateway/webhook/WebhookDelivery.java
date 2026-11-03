package com.lucas.gateway.webhook;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_deliveries")
public class WebhookDelivery {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// Só os ids, como a Charge faz com o lojista, para o pacote webhook não depender de merchant nem de charge.
	@Column(updatable = false)
	private UUID merchantId;

	@Column(updatable = false)
	private UUID chargeId;

	@Column(updatable = false)
	private String eventType;

	@Column(updatable = false)
	private String payload;

	@Enumerated(EnumType.STRING)
	private WebhookDeliveryStatus status;

	private int attempts;

	private Instant nextAttemptAt;

	private Integer lastHttpStatus;

	@Column(updatable = false)
	private Instant createdAt;

	protected WebhookDelivery() {
	}

	// Toda entrega nasce pendente e já pronta para a primeira tentativa.
	public WebhookDelivery(UUID merchantId, UUID chargeId, String eventType, String payload) {
		Instant now = Instant.now();
		this.merchantId = merchantId;
		this.chargeId = chargeId;
		this.eventType = eventType;
		this.payload = payload;
		this.status = WebhookDeliveryStatus.PENDING;
		this.attempts = 0;
		this.nextAttemptAt = now;
		this.createdAt = now;
	}

	public void markDelivered(int httpStatus) {
		finishAttempt(WebhookDeliveryStatus.DELIVERED, httpStatus);
	}

	// O status HTTP vem nulo quando não houve resposta, como num tempo esgotado ou numa conexão recusada.
	public void markFailed(Integer httpStatus) {
		finishAttempt(WebhookDeliveryStatus.FAILED, httpStatus);
	}

	// A próxima tentativa volta a nulo porque a CHECK da V6 só aceita esse campo numa entrega pendente.
	private void finishAttempt(WebhookDeliveryStatus finalStatus, Integer httpStatus) {
		this.attempts++;
		this.lastHttpStatus = httpStatus;
		this.status = finalStatus;
		this.nextAttemptAt = null;
	}

	public UUID getId() {
		return id;
	}

	public UUID getMerchantId() {
		return merchantId;
	}

	public UUID getChargeId() {
		return chargeId;
	}

	public String getEventType() {
		return eventType;
	}

	public String getPayload() {
		return payload;
	}

	public WebhookDeliveryStatus getStatus() {
		return status;
	}

	public int getAttempts() {
		return attempts;
	}

	public Instant getNextAttemptAt() {
		return nextAttemptAt;
	}

	public Integer getLastHttpStatus() {
		return lastHttpStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
