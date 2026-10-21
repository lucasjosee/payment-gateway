package com.lucas.gateway.idempotency;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

// Usa SQL direto, e não JPA como o resto do projeto, porque o mecanismo inteiro depende do INSERT ... ON CONFLICT
// do Postgres, que o JPA não tem como expressar. As datas usam o now() do banco, para a comparação de expiração
// nunca misturar o relógio da aplicação com o do banco.
@Repository
public class IdempotencyKeyRepository {

	private final JdbcClient jdbcClient;

	public IdempotencyKeyRepository(JdbcClient jdbcClient) {
		this.jdbcClient = jdbcClient;
	}

	// Devolve true quando este pedido fica com a chave: ela não existia, ou existia mas já tinha expirado.
	// Por ser um comando só, dois pedidos simultâneos com a mesma chave nunca ganham os dois. O segundo esbarra
	// na chave primária, espera o primeiro terminar o INSERT e cai no ON CONFLICT, que não altera uma chave válida.
	public boolean claim(UUID merchantId, String key, String requestHash, Duration ttl) {
		int insertedOrRenewed = jdbcClient.sql("""
				INSERT INTO idempotency_keys (merchant_id, key, request_hash, status, expires_at)
				VALUES (:merchantId, :key, :requestHash, 'IN_PROGRESS', now() + CAST(:ttl AS interval))
				ON CONFLICT (merchant_id, key) DO UPDATE
				    SET request_hash = EXCLUDED.request_hash,
				        status = 'IN_PROGRESS',
				        response_status = NULL,
				        response_body = NULL,
				        created_at = now(),
				        expires_at = EXCLUDED.expires_at
				    WHERE idempotency_keys.expires_at <= now()
				""")
				.param("merchantId", merchantId)
				.param("key", key)
				.param("requestHash", requestHash)
				// O Duration vira texto no formato ISO 8601 (PT24H), que o Postgres aceita como interval.
				.param("ttl", ttl.toString())
				.update();
		return insertedOrRenewed == 1;
	}

	public Optional<StoredKey> findValid(UUID merchantId, String key) {
		return jdbcClient.sql("""
				SELECT request_hash, status, response_status, response_body
				FROM idempotency_keys
				WHERE merchant_id = :merchantId AND key = :key AND expires_at > now()
				""")
				.param("merchantId", merchantId)
				.param("key", key)
				.query((row, rowNum) -> new StoredKey(
						row.getString("request_hash"),
						"COMPLETED".equals(row.getString("status")),
						row.getObject("response_status", Integer.class),
						row.getString("response_body")))
				.optional();
	}

	public void complete(UUID merchantId, String key, int responseStatus, String responseBody) {
		jdbcClient.sql("""
				UPDATE idempotency_keys
				SET status = 'COMPLETED', response_status = :responseStatus, response_body = :responseBody
				WHERE merchant_id = :merchantId AND key = :key AND status = 'IN_PROGRESS'
				""")
				.param("merchantId", merchantId)
				.param("key", key)
				.param("responseStatus", responseStatus)
				.param("responseBody", responseBody)
				.update();
	}

	public void release(UUID merchantId, String key) {
		jdbcClient.sql("""
				DELETE FROM idempotency_keys
				WHERE merchant_id = :merchantId AND key = :key AND status = 'IN_PROGRESS'
				""")
				.param("merchantId", merchantId)
				.param("key", key)
				.update();
	}

	// Enquanto completed for false, responseStatus e responseBody são nulos. O CHECK da tabela garante isso.
	public record StoredKey(String requestHash, boolean completed, Integer responseStatus, String responseBody) {
	}
}
