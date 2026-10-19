package com.lucas.gateway.charge.fraud;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Order(3)
public class AmountLimitRule implements FraudRule {

	// O limite é R$ 10.000,00. Uma cobrança de exatamente esse valor ainda passa.
	private static final long MAX_AMOUNT_CENTS = 1_000_000;

	@Override
	public Optional<String> evaluate(ChargeAttempt attempt) {
		if (attempt.amountCents() > MAX_AMOUNT_CENTS) {
			return Optional.of("AMOUNT_ABOVE_LIMIT");
		}
		return Optional.empty();
	}
}
