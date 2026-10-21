package com.lucas.gateway.charge;

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

// Diferente do ChargeIdempotencyTest, aqui o servidor sobe de verdade e os pedidos chegam por HTTP. Com MockMvc,
// cada pedido rodaria direto na thread do teste, sem o Tomcat no meio. Assim a disputa é a mesma de produção:
// threads do servidor brigando pelo mesmo pool de conexões e pela mesma linha no banco.
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChargeConcurrencyTest {

	private static final int THREADS = 20;

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcClient jdbcClient;

	@Test
	void onlyOneOfManySimultaneousCapturesSucceeds() throws Exception {
		RestClient http = RestClient.create("http://localhost:" + port);
		String apiKey = registerMerchant(http);
		ChargeResponse charge = createCharge(http, apiKey);
		assertThat(charge.status()).isEqualTo(ChargeStatus.AUTHORIZED);

		// Duas travas: a primeira espera todas as threads ficarem prontas, e a segunda solta todas de uma vez.
		// Sem a primeira, as últimas threads criadas poderiam largar depois de as primeiras já terem terminado.
		CountDownLatch ready = new CountDownLatch(THREADS);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<HttpStatusCode>> responses = new ArrayList<>();

		try (ExecutorService executor = Executors.newFixedThreadPool(THREADS)) {
			for (int i = 0; i < THREADS; i++) {
				responses.add(executor.submit(() -> {
					ready.countDown();
					start.await();
					return capture(http, apiKey, charge.id());
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
		assertThat(statuses).containsOnly(HttpStatus.OK, HttpStatus.CONFLICT);
		assertThat(countCaptureEvents(charge.id())).isEqualTo(1);
	}

	private String registerMerchant(RestClient http) {
		return http.post().uri("/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.body("""
						{"name": "Loja Teste", "email": "loja@teste.com"}
						""")
				.retrieve()
				.body(CreateMerchantResponse.class)
				.apiKey();
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

	// Cada captura usa uma Idempotency-Key própria. Com a mesma chave para todas, quem barraria as repetições
	// seria a idempotência, e o teste não chegaria a exercitar o @Version da Charge.
	private HttpStatusCode capture(RestClient http, String apiKey, UUID chargeId) {
		return http.post().uri("/charges/{id}/capture", chargeId)
				.header("X-Api-Key", apiKey)
				.header("Idempotency-Key", UUID.randomUUID().toString())
				.exchange((request, response) -> response.getStatusCode());
	}

	private long countCaptureEvents(UUID chargeId) {
		return jdbcClient.sql("SELECT count(*) FROM charge_events WHERE charge_id = :chargeId AND to_status = 'CAPTURED'")
				.param("chargeId", chargeId)
				.query(Long.class)
				.single();
	}
}
