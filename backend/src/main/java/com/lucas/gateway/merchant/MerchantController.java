package com.lucas.gateway.merchant;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

	private final MerchantService merchantService;

	public MerchantController(MerchantService merchantService) {
		this.merchantService = merchantService;
	}

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
