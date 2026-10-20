package com.lucas.gateway.charge;

import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Cobranças")
@RestController
@RequestMapping("/charges")
public class ChargeController {

	private final ChargeService chargeService;

	public ChargeController(ChargeService chargeService) {
		this.chargeService = chargeService;
	}

	// O 201 vale também quando o antifraude recusa. A cobrança foi criada e gravada do mesmo jeito,
	// só que com status DECLINED, e o pedido em si não tinha nada de errado para merecer um 4xx.
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ChargeResponse create(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@Valid @RequestBody CreateChargeRequest request) {
		return chargeService.create(merchant.id(), request);
	}

	@GetMapping("/{id}")
	public ChargeResponse findById(@AuthenticationPrincipal AuthenticatedMerchant merchant, @PathVariable UUID id) {
		return chargeService.findById(merchant.id(), id);
	}

	// Quando a máquina de estados não permite a mudança, a Charge lança InvalidChargeTransitionException,
	// e o ApiExceptionHandler responde 409 com os dois status na mensagem.
	@PostMapping("/{id}/capture")
	public ChargeResponse capture(@AuthenticationPrincipal AuthenticatedMerchant merchant, @PathVariable UUID id) {
		return chargeService.capture(merchant.id(), id);
	}

	@PostMapping("/{id}/cancel")
	public ChargeResponse cancel(@AuthenticationPrincipal AuthenticatedMerchant merchant, @PathVariable UUID id) {
		return chargeService.cancel(merchant.id(), id);
	}

	@PostMapping("/{id}/refund")
	public ChargeResponse refund(@AuthenticationPrincipal AuthenticatedMerchant merchant, @PathVariable UUID id) {
		return chargeService.refund(merchant.id(), id);
	}
}
