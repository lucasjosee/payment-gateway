package com.lucas.gateway.webhook;

import com.lucas.gateway.shared.NotFoundException;

public class WebhookEndpointNotFoundException extends NotFoundException {

	public WebhookEndpointNotFoundException() {
		super("Nenhum webhook cadastrado.");
	}
}
