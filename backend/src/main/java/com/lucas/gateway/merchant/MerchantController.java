package com.lucas.gateway.merchant;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Lojistas")
@RestController
@RequestMapping("/merchants")
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

	// No Swagger, toda rota exige a chave por padrão. A lista vazia tira o cadeado desta, que é pública
	// porque é por ela que o lojista recebe a primeira chave.
	@SecurityRequirements
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CreateMerchantResponse create(@Valid @RequestBody CreateMerchantRequest request) {
		return merchantService.register(request);
	}

	@GetMapping("/me")
	public MerchantResponse me(@AuthenticationPrincipal AuthenticatedMerchant merchant) {
		return merchantService.findById(merchant.id());
	}
}
