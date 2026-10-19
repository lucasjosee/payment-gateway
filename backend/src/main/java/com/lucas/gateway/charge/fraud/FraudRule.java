package com.lucas.gateway.charge.fraud;

import java.util.Optional;

public interface FraudRule {

	// Devolve o código da recusa, ou vazio quando a tentativa passa nesta regra.
	Optional<String> evaluate(ChargeAttempt attempt);
}
