package com.lucas.gateway.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "api_keys")
public class ApiKey {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	// Por padrão o @ManyToOne traz o lojista junto em toda consulta de chave.
	// Com LAZY ele só é buscado no banco quando alguém realmente usa getMerchant().
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "merchant_id")
	private Merchant merchant;

	private String prefix;

	private String keyHash;

	@Column(updatable = false)
	private Instant createdAt;

	protected ApiKey() {
	}

	public ApiKey(Merchant merchant, String prefix, String keyHash) {
		this.merchant = merchant;
		this.prefix = prefix;
		this.keyHash = keyHash;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public Merchant getMerchant() {
		return merchant;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getKeyHash() {
		return keyHash;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
