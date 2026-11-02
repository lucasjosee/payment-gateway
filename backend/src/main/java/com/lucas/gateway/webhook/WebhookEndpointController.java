package com.lucas.gateway.webhook;

import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Webhooks")
@RestController
@RequestMapping("/webhook-endpoint")
public class WebhookEndpointController {

	private final WebhookEndpointService webhookEndpointService;

	public WebhookEndpointController(WebhookEndpointService webhookEndpointService) {
		this.webhookEndpointService = webhookEndpointService;
	}

	// A rota fica no singular porque cada lojista tem um endpoint só, e o lojista vem da API key.
	@PutMapping
	public WebhookEndpointResponse register(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@Valid @RequestBody WebhookEndpointRequest request) {
		return webhookEndpointService.register(merchant.id(), request.url());
	}

	@GetMapping
	public WebhookEndpointResponse find(@AuthenticationPrincipal AuthenticatedMerchant merchant) {
		return webhookEndpointService.findByMerchant(merchant.id());
	}
}
