package com.lucas.gateway.charge;

import java.util.Set;

// Os nomes dos estados se repetem nas CHECK constraints da V2__charges.sql. Um estado novo aqui também
// precisa de uma migration, senão o banco recusa a primeira cobrança que chegar nele.
public enum ChargeStatus {

	AUTHORIZED,
	DECLINED,
	CAPTURED,
	CANCELED,
	REFUNDED;

	public boolean canTransitionTo(ChargeStatus next) {
		return allowedTransitions().contains(next);
	}

	// O switch não tem default de propósito. Se alguém criar um estado e esquecer de dizer para onde ele
	// pode ir, o código deixa de compilar, em vez de o estado novo virar um beco sem saída sem ninguém perceber.
	private Set<ChargeStatus> allowedTransitions() {
		return switch (this) {
			// Cancelar só vale enquanto o dinheiro ainda não foi capturado. Depois disso, ele volta por estorno.
			case AUTHORIZED -> Set.of(CAPTURED, CANCELED);
			case CAPTURED -> Set.of(REFUNDED);
			case DECLINED, CANCELED, REFUNDED -> Set.of();
		};
	}
}
