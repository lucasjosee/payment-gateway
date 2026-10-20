package com.lucas.gateway.charge;

import com.lucas.gateway.charge.fraud.ChargeAttempt;
import com.lucas.gateway.charge.fraud.FraudDecision;
import com.lucas.gateway.charge.fraud.FraudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChargeService {

	private final ChargeRepository chargeRepository;
	private final FraudService fraudService;
	private final CardFingerprinter cardFingerprinter;

	public ChargeService(ChargeRepository chargeRepository, FraudService fraudService,
			CardFingerprinter cardFingerprinter) {
		this.chargeRepository = chargeRepository;
		this.fraudService = fraudService;
		this.cardFingerprinter = cardFingerprinter;
	}

	@Transactional
	public ChargeResponse create(UUID merchantId, CreateChargeRequest request) {
		// O número completo só é usado nestas duas linhas. Daqui para frente circulam apenas os 4 últimos
		// dígitos e o fingerprint, e é só isso que chega ao antifraude e ao banco.
		String cardNumber = request.card().number();
		String cardLast4 = cardNumber.substring(cardNumber.length() - 4);
		String cardFingerprint = cardFingerprinter.fingerprintOf(cardNumber);

		FraudDecision decision = fraudService.evaluate(
				new ChargeAttempt(request.amountCents(), request.currency(), cardLast4, cardFingerprint));

		Charge charge = decision.isApproved()
				? Charge.authorized(merchantId, request.amountCents(), request.currency(), cardLast4, cardFingerprint)
				: Charge.declined(merchantId, request.amountCents(), request.currency(), cardLast4, cardFingerprint,
						decision.declineReason());

		return toResponse(chargeRepository.save(charge));
	}

	@Transactional(readOnly = true)
	public ChargeResponse findById(UUID merchantId, UUID chargeId) {
		return toResponse(findOwnedCharge(merchantId, chargeId));
	}

	@Transactional
	public ChargeResponse capture(UUID merchantId, UUID chargeId) {
		return transition(merchantId, chargeId, ChargeStatus.CAPTURED);
	}

	@Transactional
	public ChargeResponse cancel(UUID merchantId, UUID chargeId) {
		return transition(merchantId, chargeId, ChargeStatus.CANCELED);
	}

	@Transactional
	public ChargeResponse refund(UUID merchantId, UUID chargeId) {
		return transition(merchantId, chargeId, ChargeStatus.REFUNDED);
	}

	// Não precisa chamar save. A cobrança foi carregada dentro desta transação, então o Hibernate acompanha
	// o que muda nela e, quando a transação termina, grava sozinho o novo status e o evento que o transitionTo criou.
	private ChargeResponse transition(UUID merchantId, UUID chargeId, ChargeStatus next) {
		Charge charge = findOwnedCharge(merchantId, chargeId);
		charge.transitionTo(next);
		return toResponse(charge);
	}

	// Toda rota que recebe o id de uma cobrança busca por aqui, para nenhuma delas esquecer de conferir o dono.
	// O lojista entra no filtro da própria consulta, então a cobrança de outro lojista dá o mesmo 404 de um id
	// que não existe. Um 403 confirmaria que aquele id existe, e isso já é informação demais para um estranho.
	private Charge findOwnedCharge(UUID merchantId, UUID chargeId) {
		return chargeRepository.findByIdAndMerchantId(chargeId, merchantId)
				.orElseThrow(ChargeNotFoundException::new);
	}

	// A lista de eventos só é buscada no banco na primeira vez que alguém a lê. Por isso a conversão acontece
	// aqui, com a transação ainda aberta, e não no controller.
	private ChargeResponse toResponse(Charge charge) {
		List<ChargeResponse.Event> events = charge.getEvents().stream()
				.map(event -> new ChargeResponse.Event(event.getFromStatus(), event.getToStatus(), event.getCreatedAt()))
				.toList();
		return new ChargeResponse(charge.getId(), charge.getStatus(), charge.getAmountCents(), charge.getCurrency(),
				charge.getCardLast4(), charge.getDeclineReason(), charge.getCreatedAt(), events);
	}
}
