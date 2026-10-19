package com.lucas.gateway.charge.fraud;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Optional;

// Vem antes da regra de valor porque o limite de lá está em reais e não faria sentido para outra moeda.
@Component
@Order(1)
public class CurrencyRule implements FraudRule {

	private static final String SUPPORTED_CURRENCY = "BRL";

	@Override
	public Optional<String> evaluate(ChargeAttempt attempt) {
		if (!SUPPORTED_CURRENCY.equals(attempt.currency())) {
			return Optional.of("CURRENCY_NOT_SUPPORTED");
		}
		return Optional.empty();
	}
}
