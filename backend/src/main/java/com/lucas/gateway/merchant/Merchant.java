package com.lucas.gateway.merchant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "merchants")
public class Merchant {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	private String name;

	private String email;

	@Column(updatable = false)
	private Instant createdAt;

	protected Merchant() {
	}

	public Merchant(String name, String email) {
		this.name = name;
		this.email = email;
		this.createdAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
