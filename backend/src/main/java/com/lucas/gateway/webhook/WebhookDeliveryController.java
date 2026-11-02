package com.lucas.gateway.webhook;

import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Webhooks")
@RestController
@RequestMapping("/webhook-deliveries")
public class WebhookDeliveryController {

	private final WebhookDeliveryService webhookDeliveryService;

	public WebhookDeliveryController(WebhookDeliveryService webhookDeliveryService) {
		this.webhookDeliveryService = webhookDeliveryService;
	}

	@GetMapping
	public WebhookDeliveryPage list(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@RequestParam(required = false) UUID chargeId,
			@RequestParam(defaultValue = "0") @PositiveOrZero int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
		return webhookDeliveryService.list(merchant.id(), chargeId, page, size);
	}
}
