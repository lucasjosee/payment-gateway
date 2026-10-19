package com.lucas.gateway.charge;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "charges")
public class Charge {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// Guarda só o id, e não a entidade Merchant, para o pacote charge não depender do pacote merchant.
	// Quem garante que o lojista existe é a chave estrangeira no banco.
	@Column(updatable = false)
	private UUID merchantId;

	private long amountCents;

	private String currency;

	@Enumerated(EnumType.STRING)
	private ChargeStatus status;

	private String cardLast4;

	private String cardFingerprint;

	private String declineReason;

	@Version
	private Long version;

	@Column(updatable = false)
	private Instant createdAt;

	// Os eventos só existem dentro de uma cobrança, por isso não têm repositório próprio.
	// Gravar a cobrança grava junto os eventos novos.
	@OneToMany(mappedBy = "charge", cascade = CascadeType.ALL)
	@OrderBy("createdAt")
	private List<ChargeEvent> events = new ArrayList<>();

	protected Charge() {
	}

	private Charge(UUID merchantId, long amountCents, String currency, String cardLast4, String cardFingerprint,
			ChargeStatus initialStatus, String declineReason) {
		this.merchantId = merchantId;
		this.amountCents = amountCents;
		this.currency = currency;
		this.cardLast4 = cardLast4;
		this.cardFingerprint = cardFingerprint;
		this.status = initialStatus;
		this.declineReason = declineReason;
		this.createdAt = Instant.now();
		// A cobrança já nasce autorizada ou recusada, então o primeiro evento não tem estado de origem.
		events.add(new ChargeEvent(this, null, initialStatus));
	}

	public static Charge authorized(UUID merchantId, long amountCents, String currency, String cardLast4,
			String cardFingerprint) {
		return new Charge(merchantId, amountCents, currency, cardLast4, cardFingerprint,
				ChargeStatus.AUTHORIZED, null);
	}

	public static Charge declined(UUID merchantId, long amountCents, String currency, String cardLast4,
			String cardFingerprint, String declineReason) {
		return new Charge(merchantId, amountCents, currency, cardLast4, cardFingerprint,
				ChargeStatus.DECLINED, declineReason);
	}

	public void transitionTo(ChargeStatus next) {
		if (!status.canTransitionTo(next)) {
			throw new InvalidChargeTransitionException(status, next);
		}
		events.add(new ChargeEvent(this, status, next));
		status = next;
	}

	public UUID getId() {
		return id;
	}

	public UUID getMerchantId() {
		return merchantId;
	}

	public long getAmountCents() {
		return amountCents;
	}

	public String getCurrency() {
		return currency;
	}

	public ChargeStatus getStatus() {
		return status;
	}

	public String getCardLast4() {
		return cardLast4;
	}

	public String getDeclineReason() {
		return declineReason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	// Quem está de fora só lê o histórico. O único jeito de acrescentar um evento é pelo transitionTo.
	public List<ChargeEvent> getEvents() {
		return Collections.unmodifiableList(events);
	}
}
