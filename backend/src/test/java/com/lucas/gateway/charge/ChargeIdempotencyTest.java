package com.lucas.gateway.charge;

import com.lucas.gateway.merchant.CreateMerchantResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

// A classe não é @Transactional de propósito. Com MockMvc, o pedido roda na mesma thread do teste, e as transações
// que o IdempotencyService abre por conta própria entrariam na do teste. O teste deixaria de reproduzir o que
// acontece em produção.
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class ChargeIdempotencyTest {

	// A mesma imagem do docker-compose.yml, para o teste rodar na mesma versão do Postgres que a aplicação usa.
	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private MockMvcTester mvc;

	@Autowired
	private JsonMapper jsonMapper;

	@Autowired
	private JdbcClient jdbcClient;

	private CreateMerchantResponse merchant;

	// Cada teste cadastra o próprio lojista, para a contagem de cobranças de um teste não enxergar as do outro.
	@BeforeEach
	void registerMerchant() {
		MvcTestResult result = mvc.post().uri("/merchants")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"name": "Loja Teste", "email": "%s@teste.com"}
						""".formatted(UUID.randomUUID()))
				.exchange();

		assertThat(result).hasStatus(HttpStatus.CREATED);
		merchant = read(result, CreateMerchantResponse.class);
	}

	@Test
	void sameKeyAndSameBodyReturnTheSameChargeWithoutCreatingAnother() {
		String key = UUID.randomUUID().toString();

		MvcTestResult first = createCharge(key, 1500);
		MvcTestResult second = createCharge(key, 1500);

		assertThat(first).hasStatus(HttpStatus.CREATED);
		assertThat(second).hasStatus(HttpStatus.CREATED);
		assertThat(read(second, ChargeResponse.class)).isEqualTo(read(first, ChargeResponse.class));
		assertThat(countChargesOfMerchant()).isEqualTo(1);
	}

	@Test
	void sameKeyWithDifferentBodyIsRejected() {
		String key = UUID.randomUUID().toString();

		MvcTestResult first = createCharge(key, 1500);
		MvcTestResult second = createCharge(key, 9900);

		assertThat(first).hasStatus(HttpStatus.CREATED);
		assertThat(second).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
		assertThat(countChargesOfMerchant()).isEqualTo(1);
	}

	private MvcTestResult createCharge(String idempotencyKey, long amountCents) {
		return mvc.post().uri("/charges")
				.header("X-Api-Key", merchant.apiKey())
				.header("Idempotency-Key", idempotencyKey)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
						{
						  "amountCents": %d,
						  "currency": "BRL",
						  "card": {"number": "4111111111111111", "expiry": "2030-12", "cvv": "123"}
						}
						""".formatted(amountCents))
				.exchange();
	}

	private long countChargesOfMerchant() {
		return jdbcClient.sql("SELECT count(*) FROM charges WHERE merchant_id = :merchantId")
				.param("merchantId", merchant.id())
				.query(Long.class)
				.single();
	}

	private <T> T read(MvcTestResult result, Class<T> type) {
		return jsonMapper.readValue(result.getResponse().getContentAsByteArray(), type);
	}
}
