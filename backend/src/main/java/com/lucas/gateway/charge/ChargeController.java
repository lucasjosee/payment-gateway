package com.lucas.gateway.charge;

import com.lucas.gateway.idempotency.IdempotencyService;
import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.function.Supplier;

@Tag(name = "Cobranças")
@RestController
@RequestMapping("/charges")
public class ChargeController {

	private static final String IDEMPOTENCY_KEY = "Idempotency-Key";

	private final ChargeService chargeService;
	private final IdempotencyService idempotencyService;

	public ChargeController(ChargeService chargeService, IdempotencyService idempotencyService) {
		this.chargeService = chargeService;
		this.idempotencyService = idempotencyService;
	}

	// O 201 vale também quando o antifraude recusa. A cobrança foi criada e gravada do mesmo jeito,
	// só que com status DECLINED, e o pedido em si não tinha nada de errado para merecer um 4xx.
	// O status vai no ResponseEntity, e não em @ResponseStatus, porque a idempotência guarda o status junto com
	// o corpo para devolver o mesmo 201 numa repetição. O @ApiResponse serve só para o Swagger mostrar esse 201.
	@PostMapping
	@ApiResponse(responseCode = "201")
	public ResponseEntity<ChargeResponse> create(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey, @Valid @RequestBody CreateChargeRequest request,
			HttpServletRequest httpRequest) {
		return idempotencyService.execute(merchant.id(), idempotencyKey, httpRequest, request, ChargeResponse.class,
				() -> ResponseEntity.status(HttpStatus.CREATED).body(chargeService.create(merchant.id(), request)));
	}

	@GetMapping
	public ChargePage list(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return chargeService.list(merchant.id(), page, size);
	}

	@GetMapping("/{id}")
	public ChargeResponse findById(@AuthenticationPrincipal AuthenticatedMerchant merchant, @PathVariable UUID id) {
		return chargeService.findById(merchant.id(), id);
	}

	// Quando a máquina de estados não permite a mudança, a Charge lança InvalidChargeTransitionException,
	// e o ApiExceptionHandler responde 409 com os dois status na mensagem.
	@PostMapping("/{id}/capture")
	public ResponseEntity<ChargeResponse> capture(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey, @PathVariable UUID id,
			HttpServletRequest httpRequest) {
		return executeAction(merchant, idempotencyKey, httpRequest, () -> chargeService.capture(merchant.id(), id));
	}

	@PostMapping("/{id}/cancel")
	public ResponseEntity<ChargeResponse> cancel(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey, @PathVariable UUID id,
			HttpServletRequest httpRequest) {
		return executeAction(merchant, idempotencyKey, httpRequest, () -> chargeService.cancel(merchant.id(), id));
	}

	@PostMapping("/{id}/refund")
	public ResponseEntity<ChargeResponse> refund(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestHeader(IDEMPOTENCY_KEY) String idempotencyKey, @PathVariable UUID id,
			HttpServletRequest httpRequest) {
		return executeAction(merchant, idempotencyKey, httpRequest, () -> chargeService.refund(merchant.id(), id));
	}

	// As ações não têm corpo, então o body vai nulo. O que separa um /capture de um /refund com a mesma chave
	// é a rota, que o RequestHasher já põe no hash.
	private ResponseEntity<ChargeResponse> executeAction(AuthenticatedMerchant merchant, String idempotencyKey,
			HttpServletRequest httpRequest, Supplier<ChargeResponse> action) {
		return idempotencyService.execute(merchant.id(), idempotencyKey, httpRequest, null, ChargeResponse.class,
				() -> ResponseEntity.ok(action.get()));
	}
}
