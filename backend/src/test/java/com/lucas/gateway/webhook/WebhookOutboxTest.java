package com.lucas.gateway.webhook;

import com.lucas.gateway.charge.ChargeResponse;
import com.lucas.gateway.merchant.CreateMerchantResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

// A mesma disputa do ChargeConcurrencyTest, agora olhando as entregas. Várias capturas passam pela máquina de estados
// e pelo listener antes de o lock otimista barrar as atrasadas no commit. A entrega gravada por cada uma delas
// precisa sumir junto com a captura desfeita.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebhookOutboxTest {

	private static final int THREADS = 20;

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@LocalServerPort
	private int port;

	@Autowired
	private WebhookEndpointRepository webhookEndpointRepository;

	@Autowired
	private JdbcClient jdbcClient;

	@Test
	void manySimultaneousCapturesLeaveExactlyOneCapturedDelivery() throws Exception {
		RestClient http = RestClient.create("http://localhost:" + port + "/api");
		CreateMerchantResponse merchant = registerMerchant(http);
		ChargeResponse charge = createCharge(http, merchant.apiKey());
		// Cadastrado só depois da cobrança, para a única entrega esperada ser a da captura. Vai direto pelo repositório,
		// e não pelo PUT /webhook-endpoint, porque lá a URL passaria pelo validador, e um host de verdade faria o teste
		// depender do DNS. Nada é enviado para ela: o despachante fica desligado nos testes.
		webhookEndpointRepository.save(new WebhookEndpoint(merchant.id(), "https://loja.example/webhook"));

		CountDownLatch ready = new CountDownLatch(THREADS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<HttpStatusCode>> responses = new ArrayList<>();

		try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
			for (int i = 0; i < THREADS; i++) {
				responses.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return capture(http, merchant.apiKey(), charge.id());
				}));
			}
			ready.await();
			start.countDown();
		}

		List<HttpStatusCode> statuses = new ArrayList<>();
		for (Future<HttpStatusCode> response : responses) {
			statuses.add(response.get());
		}
		assertThat(statuses).containsOnlyOnce(HttpStatus.OK);
		assertThat(eventTypesOf(charge.id())).containsExactly("charge.captured");
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

	private ChargeResponse createCharge(RestClient http, String apiKey) {
		return http.post().uri("/charges")
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
				.body(ChargeResponse.class);
	}

	private HttpStatusCode capture(RestClient http, String apiKey, UUID chargeId) {
		return http.post().uri("/charges/{id}/capture", chargeId)
				.header("X-Api-Key", apiKey)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.exchange((request, response) -> response.getStatusCode());
	}

	private List<String> eventTypesOf(UUID chargeId) {
		return jdbcClient.sql("SELECT event_type FROM webhook_deliveries WHERE charge_id = :chargeId")
				.param("chargeId", chargeId)
				.query(String.class)
				.list();
	}
}
