package com.lucas.gateway.charge.fraud;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FraudService {

	private final List<FraudRule> rules;

	// O Spring entrega as regras ordenadas pelo @Order de cada uma. A ordem decide qual motivo aparece
	// quando mais de uma regra recusaria a mesma tentativa.
	public FraudService(List<FraudRule> rules) {
		this.rules = rules;
	}

	public FraudDecision evaluate(ChargeAttempt attempt) {
		// O stream avalia uma regra de cada vez e para na primeira recusa. As regras seguintes nem rodam.
		return rules.stream()
				.map(rule -> rule.evaluate(attempt))
				.flatMap(Optional::stream)
				.findFirst()
				.map(FraudDecision::decline)
				.orElseGet(FraudDecision::approve);
	}
}
