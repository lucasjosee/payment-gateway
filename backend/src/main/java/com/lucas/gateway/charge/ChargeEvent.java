package com.lucas.gateway.charge;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "charge_events")
public class ChargeEvent {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "charge_id")
	private Charge charge;

	@Enumerated(EnumType.STRING)
	private ChargeStatus fromStatus;

	@Enumerated(EnumType.STRING)
	private ChargeStatus toStatus;

	@Column(updatable = false)
	private Instant createdAt;

	protected ChargeEvent() {
	}

	// O construtor não é público porque só a Charge cria eventos, sempre junto com a mudança de estado.
	ChargeEvent(Charge charge, ChargeStatus fromStatus, ChargeStatus toStatus) {
		this.charge = charge;
		this.fromStatus = fromStatus;
		this.toStatus = toStatus;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public ChargeStatus getFromStatus() {
		return fromStatus;
	}

	public ChargeStatus getToStatus() {
		return toStatus;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
