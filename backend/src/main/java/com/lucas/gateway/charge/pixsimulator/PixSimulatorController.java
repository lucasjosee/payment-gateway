package com.lucas.gateway.charge.pixsimulator;

import com.lucas.gateway.charge.ChargeService;
import com.lucas.gateway.charge.PixCharge;
import com.lucas.gateway.merchant.MerchantService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

// O lado de quem paga, no lugar do app do banco. Não há banco de verdade por trás, e por isso só existe na demonstração.
@Tag(name = "Desenvolvimento")
@Profile("demo")
@RestController
@RequestMapping("/dev/pix")
public class PixSimulatorController {

	private final ChargeService chargeService;
	private final MerchantService merchantService;

	public PixSimulatorController(ChargeService chargeService, MerchantService merchantService) {
		this.chargeService = chargeService;
		this.merchantService = merchantService;
	}

	@GetMapping("/{chargeId}")
	@SecurityRequirements
	public PixPaymentResponse find(@PathVariable UUID chargeId) {
		return toResponse(chargeService.findPix(chargeId));
	}

	// Sem Idempotency-Key de propósito: pagar duas vezes não devolve a resposta guardada, e o segundo pedido dá 409.
	@PostMapping("/{chargeId}/pay")
	@SecurityRequirements
	public PixPaymentResponse pay(@PathVariable UUID chargeId) {
		return toResponse(chargeService.payPix(chargeId));
	}

	private PixPaymentResponse toResponse(PixCharge pix) {
		String merchantName = merchantService.findById(pix.merchantId()).name();
		return new PixPaymentResponse(pix.amountCents(), pix.currency(), merchantName, pix.status(), pix.expiresAt());
	}
}
