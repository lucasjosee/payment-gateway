package com.lucas.gateway.webhook;

import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

// Faz a chamada HTTP para o servidor do lojista. Da resposta, só o status interessa, e o corpo nem é lido.
@Component
public class WebhookSender {

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
	int send(String url, String payload) {
		return restClient.post()
				.uri(URI.create(url))
				.contentType(MediaType.APPLICATION_JSON)
				.body(payload.getBytes(StandardCharsets.UTF_8))
				.exchange((request, response) -> response.getStatusCode().value());
	}
}
