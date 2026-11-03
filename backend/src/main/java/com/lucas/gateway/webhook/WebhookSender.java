package com.lucas.gateway.webhook;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

// Faz a chamada HTTP para o servidor do lojista, assinada para ele poder conferir que ela veio do gateway. Da
// resposta, só o status interessa, e o corpo nem é lido.
@Component
public class WebhookSender {

	private static final String EVENT_ID_HEADER = "Ponte-Event-Id";
	private static final String SIGNATURE_HEADER = "Ponte-Signature";
	private static final String ALGORITHM = "HmacSHA256";

	// Curtos porque a trava da entrega e uma conexão com o banco ficam presas enquanto o envio dura.
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(2);
	private static final Duration RESPONSE_TIMEOUT = Duration.ofSeconds(5);

	private final RestClient restClient;

	public WebhookSender() {
		// Seguir um redirecionamento mandaria o evento para um endereço que o lojista não cadastrou.
		HttpClient httpClient = HttpClient.newBuilder()
				.connectTimeout(CONNECT_TIMEOUT)
				.followRedirects(HttpClient.Redirect.NEVER)
				.build();
		JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
		requestFactory.setReadTimeout(RESPONSE_TIMEOUT);
		this.restClient = RestClient.builder().requestFactory(requestFactory).build();
	}

	// Devolve o status HTTP, qualquer que seja. Sem resposta, lança ResourceAccessException. O URI.create evita que
	// o RestClient trate a URL como um template e tente preencher um trecho entre chaves.
	int send(String url, UUID eventId, String signingSecret, String payload) {
		byte[] body = payload.getBytes(StandardCharsets.UTF_8);
		return restClient.post()
				.uri(URI.create(url))
				.contentType(MediaType.APPLICATION_JSON)
				.header(EVENT_ID_HEADER, eventId.toString())
				.header(SIGNATURE_HEADER, signatureOf(signingSecret, body))
				.body(body)
				.exchange((request, response) -> response.getStatusCode().value());
	}

	// O timestamp é o do envio, e não o da criação da entrega, porque ela pode sair bem depois de criada e o lojista
	// recusa assinaturas com mais de 5 minutos. A chave é o segredo inteiro, com o whsec_, como o lojista o copia.
	private static String signatureOf(String signingSecret, byte[] body) {
		long timestamp = Instant.now().getEpochSecond();
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), ALGORITHM));
			mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
			return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(mac.doFinal(body));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer HmacSHA256", e);
		}
	}
}
