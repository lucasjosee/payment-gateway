package com.lucas.gateway.webhook;

import com.lucas.gateway.shared.UnprocessableException;

// As mensagens nunca repetem a URL nem os endereços para onde o host resolve: devolver esses endereços ensinaria a
// quem testa nomes como está organizada a rede por dentro.
public class InvalidWebhookUrlException extends UnprocessableException {

	InvalidWebhookUrlException(String message) {
		super(message);
	}
}
