package com.lucas.gateway.webhook;

import com.lucas.gateway.merchant.CreateMerchantResponse;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

// O despachante envia pela rede para um servidor HTTP que faz o papel do lojista. O validador recusaria o 127.0.0.1
// desse servidor, então ele é liberado pela mesma propriedade que libera o receptor de teste no profile demo.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
		properties = "gateway.webhook-retry-delays=100ms, 200ms")
class WebhookRetryTest {

	private static final int FAILURES_BEFORE_SUCCESS = 2;

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	private static final List<ReceivedRequest> received = new CopyOnWriteArrayList<>();
	private static HttpServer merchantServer;

	@LocalServerPort
	private int port;

	@Autowired
	private WebhookDispatcher dispatcher;

	@Autowired
	private WebhookDeliveryRepository webhookDeliveryRepository;

	@BeforeAll
	static void startMerchantServer() throws IOException {
		merchantServer = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
		merchantServer.createContext("/webhook", exchange -> {
			Headers headers = exchange.getRequestHeaders();
			received.add(new ReceivedRequest(Instant.now(), headers.getFirst("Ponte-Event-Id"),
					headers.getFirst("Ponte-Signature"), exchange.getRequestBody().readAllBytes()));
			exchange.sendResponseHeaders(received.size() <= FAILURES_BEFORE_SUCCESS ? 503 : 200, -1);
			exchange.close();
		});
		merchantServer.start();
	}

	@AfterAll
	static void stopMerchantServer() {
		merchantServer.stop(0);
	}

	@DynamicPropertySource
	static void exemptMerchantServer(DynamicPropertyRegistry registry) {
		registry.add("gateway.webhook-receiver-base-url", WebhookRetryTest::merchantServerUrl);
	}

	private static String merchantServerUrl() {
		return "http://127.0.0.1:" + merchantServer.getAddress().getPort() + "/webhook";
	}

	@Test
	void deliversOnTheThirdAttemptAfterTwoFailures() throws Exception {
		RestClient http = RestClient.create("http://localhost:" + port + "/api");
		CreateMerchantResponse merchant = registerMerchant(http);
		WebhookEndpointResponse endpoint = registerWebhook(http, merchant.apiKey(),
				merchantServerUrl() + "/" + merchant.id());
		createCharge(http, merchant.apiKey());

		WebhookDelivery delivery = dispatchUntilFinished();

		assertThat(delivery.getStatus()).isEqualTo(WebhookDeliveryStatus.DELIVERED);
		assertThat(delivery.getAttempts()).isEqualTo(3);
		assertThat(delivery.getLastHttpStatus()).isEqualTo(200);
		assertThat(received).hasSize(3).allSatisfy(request -> {
			assertThat(request.eventId()).isEqualTo(delivery.getId().toString());
			assertThat(new String(request.body(), StandardCharsets.UTF_8)).isEqualTo(delivery.getPayload());
			assertThat(request.signature()).isEqualTo(expectedSignature(endpoint.signingSecret(), request));
		});
		// As esperas são mínimas, e não exatas: a tentativa seguinte só sai na primeira rodada depois de vencida.
		assertThat(Duration.between(received.get(0).at(), received.get(1).at()))
				.isGreaterThanOrEqualTo(Duration.ofMillis(100));
		assertThat(Duration.between(received.get(1).at(), received.get(2).at()))
				.isGreaterThanOrEqualTo(Duration.ofMillis(200));
	}

	// Faz o papel do agendamento, que fica desligado nos testes: roda o despachante até a entrega sair de PENDING.
	private WebhookDelivery dispatchUntilFinished() throws InterruptedException {
		Instant deadline = Instant.now().plusSeconds(10);
		while (Instant.now().isBefore(deadline)) {
			dispatcher.dispatchDue();
			WebhookDelivery delivery = webhookDeliveryRepository.findAll().getFirst();
			if (delivery.getStatus() != WebhookDeliveryStatus.PENDING) {
				return delivery;
			}
			Thread.sleep(50);
		}
		throw new AssertionError("A entrega continuou PENDING depois de 10 s");
	}

	// Calculada aqui de novo, como o lojista faria, sem usar o WebhookSender. Um teste que repetisse o código do envio
	// aceitaria qualquer erro que estivesse nele.
	private static String expectedSignature(String signingSecret, ReceivedRequest request)
			throws GeneralSecurityException {
		String timestamp = request.signature().substring("t=".length(), request.signature().indexOf(','));
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
		return "t=" + timestamp + ",v1=" + HexFormat.of().formatHex(mac.doFinal(request.body()));
	}

	private CreateMerchantResponse registerMerchant(RestClient http) {
		return http.post().uri("/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"name": "Loja Teste", "email": "loja@teste.com"}
						""")
				.retrieve()
				.body(CreateMerchantResponse.class);
	}

	private WebhookEndpointResponse registerWebhook(RestClient http, String apiKey, String url) {
		return http.put().uri("/webhook-endpoint")
				.header("X-Api-Key", apiKey)
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"url": "%s"}
						""".formatted(url))
				.retrieve()
				.body(WebhookEndpointResponse.class);
	}

	private void createCharge(RestClient http, String apiKey) {
		http.post().uri("/charges")
				.header("X-Api-Key", apiKey)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{
						  "amountCents": 1500,
						  "currency": "BRL",
						  "card": {"number": "4111111111111111", "expiry": "2030-12", "cvv": "123"}
						}
						""")
				.retrieve()
				.toBodilessEntity();
	}

	private record ReceivedRequest(Instant at, String eventId, String signature, byte[] body) {
	}
}
