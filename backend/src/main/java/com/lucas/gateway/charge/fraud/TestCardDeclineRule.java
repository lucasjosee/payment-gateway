package com.lucas.gateway.charge.fraud;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Imita o cartão de teste que os gateways reais oferecem para simular a recusa do banco emissor.
// Assim dá para mostrar uma recusa na demo sem precisar de um cartão bloqueado de verdade.
@Component
@Order(2)
public class TestCardDeclineRule implements FraudRule {

	private static final String DECLINED_TEST_CARD_LAST4 = "0002";

	@Override
	public Optional<String> evaluate(ChargeAttempt attempt) {
		if (DECLINED_TEST_CARD_LAST4.equals(attempt.cardLast4())) {
			return Optional.of("CARD_DECLINED");
		}
		return Optional.empty();
	}
}
