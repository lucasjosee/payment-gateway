package com.lucas.gateway.webhook.receiver;

import com.lucas.gateway.webhook.WebhookEndpointRepository;
import com.lucas.gateway.webhook.WebhookSigningSecrets;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Confere o header Ponte-Signature como o servidor de um lojista faria. Não reaproveita o código do WebhookSender de
// propósito: refaz a conta a partir do formato combinado, e assim um erro nesse formato aparece aqui em vez de se
// repetir dos dois lados.
@Component
@Profile("demo")
public class WebhookSignatureVerifier {

	private static final String ALGORITHM = "HmacSHA256";
	private static final Duration TOLERANCE = Duration.ofMinutes(5);

	// O limite de 12 dígitos impede que um número enorme estoure o Long.parseLong.
	private static final Pattern SIGNATURE = Pattern.compile("t=(\\d{1,12}),v1=([0-9a-f]{64})");

	private final WebhookEndpointRepository webhookEndpointRepository;
	private final WebhookSigningSecrets signingSecrets;

	public WebhookSignatureVerifier(WebhookEndpointRepository webhookEndpointRepository,
			WebhookSigningSecrets signingSecrets) {
		this.webhookEndpointRepository = webhookEndpointRepository;
		this.signingSecrets = signingSecrets;
	}

	boolean isValid(UUID merchantId, String signatureHeader, byte[] body) {
		if (signatureHeader == null) {
			return false;
		}
		Matcher matcher = SIGNATURE.matcher(signatureHeader);
		if (!matcher.matches()) {
			return false;
		}
		String timestamp = matcher.group(1);
		if (Math.abs(Instant.now().getEpochSecond() - Long.parseLong(timestamp)) > TOLERANCE.toSeconds()) {
			return false;
		}
		Optional<String> secret = secretOf(merchantId);
		if (secret.isEmpty()) {
			return false;
		}
		byte[] expected = hmacHex(secret.get(), timestamp, body).getBytes(StandardCharsets.US_ASCII);
		byte[] received = matcher.group(2).getBytes(StandardCharsets.US_ASCII);
		// Um equals pararia no primeiro byte diferente, e o tempo da resposta diria a quem tenta forjar a assinatura
		// quantos bytes do começo ele já acertou. O isEqual compara todos.
		return MessageDigest.isEqual(expected, received);
	}

	// Um lojista de verdade teria o segredo na própria configuração. Como o receptor roda dentro da API, ele o calcula.
	private Optional<String> secretOf(UUID merchantId) {
		return webhookEndpointRepository.findByMerchantId(merchantId)
				.map(endpoint -> signingSecrets.secretFor(endpoint.getId()));
	}

	private static String hmacHex(String secret, String timestamp, byte[] body) {
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
			mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(mac.doFinal(body));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer HmacSHA256", e);
		}
	}
}
