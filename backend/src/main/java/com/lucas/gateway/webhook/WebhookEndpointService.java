package com.lucas.gateway.webhook;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WebhookEndpointService {

	private final WebhookEndpointRepository webhookEndpointRepository;
	private final WebhookSigningSecrets signingSecrets;
	private final WebhookUrlValidator urlValidator;

	public WebhookEndpointService(WebhookEndpointRepository webhookEndpointRepository,
			WebhookSigningSecrets signingSecrets, WebhookUrlValidator urlValidator) {
		this.webhookEndpointRepository = webhookEndpointRepository;
		this.signingSecrets = signingSecrets;
		this.urlValidator = urlValidator;
	}

	// Trocar a URL altera a linha que já existe em vez de criar outra, então o endpoint mantém o mesmo id.
	@Transactional
	public WebhookEndpointResponse register(UUID merchantId, String url) {
		urlValidator.validate(merchantId, url);
		WebhookEndpoint endpoint = webhookEndpointRepository.findByMerchantId(merchantId).orElse(null);
		if (endpoint == null) {
			endpoint = webhookEndpointRepository.save(new WebhookEndpoint(merchantId, url));
		} else {
			endpoint.changeUrl(url);
		}
		return toResponse(endpoint);
	}

	@Transactional(readOnly = true)
	public WebhookEndpointResponse findByMerchant(UUID merchantId) {
		return webhookEndpointRepository.findByMerchantId(merchantId)
				.map(this::toResponse)
				.orElseThrow(WebhookEndpointNotFoundException::new);
	}

	private WebhookEndpointResponse toResponse(WebhookEndpoint endpoint) {
		return new WebhookEndpointResponse(endpoint.getUrl(), signingSecrets.secretFor(endpoint.getId()));
	}
}
