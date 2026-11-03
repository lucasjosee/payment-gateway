package com.lucas.gateway.webhook.receiver;

import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

@Tag(name = "Desenvolvimento")
@Profile("demo")
@RestController
@RequestMapping("/dev/webhook-receiver")
public class WebhookReceiverController {

	private final WebhookReceiver receiver;

	public WebhookReceiverController(WebhookReceiver receiver) {
		this.receiver = receiver;
	}

	// Público, como o endpoint de um lojista de verdade, e por isso liberado na SecurityConfig. O corpo é lido direto
	// do stream e só até o limite: com @RequestBody, o Spring guardaria o corpo inteiro na memória antes de conferir.
	@PostMapping("/{merchantId}")
	@SecurityRequirements
	public ResponseEntity<Void> receive(@PathVariable UUID merchantId, InputStream body) throws IOException {
		if (receiver.isOffline(merchantId)) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
		}
		byte[] bytes = body.readNBytes(WebhookReceiver.MAX_BODY_BYTES + 1);
		if (bytes.length > WebhookReceiver.MAX_BODY_BYTES) {
			return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).build();
		}
		receiver.record(merchantId, new ReceivedWebhook(Instant.now(), new String(bytes, StandardCharsets.UTF_8)));
		return ResponseEntity.ok().build();
	}

	@GetMapping
	public WebhookReceiverResponse find(@AuthenticationPrincipal AuthenticatedMerchant merchant) {
		return receiver.describe(merchant.id());
	}

	@PutMapping("/offline")
	public WebhookReceiverResponse setOffline(@AuthenticationPrincipal AuthenticatedMerchant merchant,
			@Valid @RequestBody OfflineModeRequest request) {
		receiver.setOffline(merchant.id(), request.offline());
		return receiver.describe(merchant.id());
	}
}
