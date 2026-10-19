package com.lucas.gateway.charge.fraud;

import com.lucas.gateway.charge.ChargeRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

// Fica por último porque é a única regra que consulta o banco. Se uma regra anterior já recusou,
// a consulta nem chega a acontecer.
@Component
@Order(4)
public class CardVelocityRule implements FraudRule {

	private static final int MAX_CHARGES_IN_WINDOW = 3;
	private static final Duration WINDOW = Duration.ofMinutes(5);

	private final ChargeRepository chargeRepository;

	public CardVelocityRule(ChargeRepository chargeRepository) {
		this.chargeRepository = chargeRepository;
	}

	@Override
	public Optional<String> evaluate(ChargeAttempt attempt) {
		// A contagem pega cobranças de todos os lojistas, porque quem testa cartão roubado espalha as tentativas
		// por várias lojas. As recusadas também contam: cada tentativa que falhou pesa contra o cartão.
		// A tentativa atual ainda não foi gravada, então três cobranças recentes já bastam para recusar a quarta.
		long recentCharges = chargeRepository.countByCardFingerprintAndCreatedAtAfter(
				attempt.cardFingerprint(), Instant.now().minus(WINDOW));
		if (recentCharges >= MAX_CHARGES_IN_WINDOW) {
			return Optional.of("CARD_VELOCITY_EXCEEDED");
		}
		return Optional.empty();
	}
}
