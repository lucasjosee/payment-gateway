package com.lucas.gateway.webhook;

public record WebhookEndpointResponse(String url, String signingSecret) {

	// O toString padrão de um record imprime todos os campos, e um log deste objeto exporia o segredo.
	@Override
	public String toString() {
		return "WebhookEndpointResponse[url=%s, signingSecret=****]".formatted(url);
	}
}
