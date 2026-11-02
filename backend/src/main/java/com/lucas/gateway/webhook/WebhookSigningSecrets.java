package com.lucas.gateway.webhook;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.UUID;

// O segredo não fica no banco: ele é recalculado sempre que preciso, a partir do id do endpoint e da chave da
// aplicação. Como trocar a URL mantém o id, o segredo também se mantém.
@Component
public class WebhookSigningSecrets {

	private static final String ALGORITHM = "HmacSHA256";
	private static final String MARKER = "whsec_";

	private final SecretKeySpec key;

	public WebhookSigningSecrets(@Value("${gateway.webhook-signing-key}") String key) {
		this.key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
	}

	public String secretFor(UUID endpointId) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(key);
			byte[] secret = mac.doFinal(endpointId.toString().getBytes(StandardCharsets.UTF_8));
			return MARKER + Base64.getUrlEncoder().withoutPadding().encodeToString(secret);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer HmacSHA256", e);
		}
	}
}
