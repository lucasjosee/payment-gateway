package com.lucas.gateway.webhook;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// O @Pattern só confere o formato: esquema http ou https, um host e nada de espaços.
public record WebhookEndpointRequest(
		@NotBlank @Size(max = 2048)
		@Pattern(regexp = "https?://[^\\s/?#]+([/?#]\\S*)?",
				message = "deve ser uma URL completa, começando com http:// ou https://")
		String url) {
}
