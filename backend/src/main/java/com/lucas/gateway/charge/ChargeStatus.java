package com.lucas.gateway.charge;

import java.util.Set;

// Os nomes dos estados se repetem nas CHECK constraints da V7__pix.sql. Um estado novo aqui também
// precisa de uma migration, senão o banco recusa a primeira cobrança que chegar nele.
public enum ChargeStatus {

	AUTHORIZED,
	DECLINED,
	CAPTURED,
	CANCELED,
	REFUNDED,
	PENDING,
	PAID,
	EXPIRED;

	public boolean canTransitionTo(ChargeStatus next) {
		return allowedTransitions().contains(next);
	}

	// O switch não tem default de propósito. Se alguém criar um estado e esquecer de dizer para onde ele
	// pode ir, o código deixa de compilar, em vez de o estado novo virar um beco sem saída sem ninguém perceber.
	private Set<ChargeStatus> allowedTransitions() {
		return switch (this) {
			// Cancelar só vale enquanto o dinheiro ainda não entrou. Depois disso, ele volta por estorno.
			case AUTHORIZED -> Set.of(CAPTURED, CANCELED);
			case PENDING -> Set.of(PAID, CANCELED, EXPIRED);
			case CAPTURED, PAID -> Set.of(REFUNDED);
			case DECLINED, CANCELED, REFUNDED, EXPIRED -> Set.of();
		};
	}
}
