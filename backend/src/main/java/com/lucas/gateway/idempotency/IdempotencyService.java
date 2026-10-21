package com.lucas.gateway.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.UUID;
import java.util.function.Supplier;

// Garante que um pedido repetido com a mesma Idempotency-Key seja executado uma vez só. O controller chama o
// execute fora de qualquer transação, porque cada etapa daqui precisa controlar a própria: a chave em andamento
// tem que ficar visível para os outros pedidos antes de a ação começar.
@Service
public class IdempotencyService {

	private static final Logger log = LoggerFactory.getLogger(IdempotencyService.class);

	// O mesmo prazo do Stripe, que cobre as retentativas automáticas de um cliente ao longo de um dia inteiro.
	private static final Duration KEY_TTL = Duration.ofHours(24);

	// O mesmo limite da coluna key da V3__idempotency_keys.sql. Sem esta barreira, uma chave maior só seria
	// recusada pelo banco, no meio do INSERT, e o cliente receberia um 500 em vez de um 400.
	private static final int KEY_MAX_LENGTH = 255;

	private final IdempotencyKeyRepository repository;
	private final RequestHasher requestHasher;
	private final TransactionTemplate transactionTemplate;
	private final JsonMapper jsonMapper;

	public IdempotencyService(IdempotencyKeyRepository repository, RequestHasher requestHasher,
			TransactionTemplate transactionTemplate, JsonMapper jsonMapper) {
		this.repository = repository;
		this.requestHasher = requestHasher;
		this.transactionTemplate = transactionTemplate;
		this.jsonMapper = jsonMapper;
	}

	public <T> ResponseEntity<T> execute(UUID merchantId, String key, HttpServletRequest httpRequest, Object body,
			Class<T> responseType, Supplier<ResponseEntity<T>> action) {
		if (key.isBlank() || key.length() > KEY_MAX_LENGTH) {
			throw new InvalidIdempotencyKeyException(KEY_MAX_LENGTH);
		}

		String requestHash = requestHasher.hash(httpRequest.getMethod(), httpRequest.getRequestURI(), body);

		if (!repository.claim(merchantId, key, requestHash, KEY_TTL)) {
			return replay(merchantId, key, requestHash, responseType);
		}
		// A chave em si não vai para o log: ela é escolhida pelo cliente e poderia trazer qualquer texto.
		log.info("Idempotency-Key nova, reservada para este pedido");

		try {
			// A ação e a gravação da resposta acontecem na mesma transação, e o @Transactional do service chamado
			// pela ação entra nela em vez de abrir outra. Assim, nunca existe uma cobrança gravada sem a resposta
			// guardada, nem uma resposta guardada sem a cobrança.
			return transactionTemplate.execute(status -> {
				ResponseEntity<T> response = action.get();
				repository.complete(merchantId, key, response.getStatusCode().value(),
						jsonMapper.writeValueAsString(response.getBody()));
				return response;
			});
		} catch (RuntimeException failure) {
			// Os erros da API sempre saem como exceção, então só respostas de sucesso chegam a ser guardadas.
			// Num erro, a chave é liberada para o cliente poder tentar de novo com ela, e o erro segue para o
			// ApiExceptionHandler como se a idempotência não existisse.
			releaseAfterFailure(merchantId, key, failure);
			throw failure;
		}
	}

	private <T> ResponseEntity<T> replay(UUID merchantId, String key, String requestHash, Class<T> responseType) {
		// Se a chave sumiu entre o claim e esta leitura, o pedido que estava com ela acabou de falhar e liberá-la.
		// Responder "em andamento" faz o cliente tentar de novo, e na próxima tentativa ela já estará livre.
		IdempotencyKeyRepository.StoredKey stored = repository.findValid(merchantId, key)
				.orElseThrow(IdempotencyKeyInProgressException::new);

		if (!stored.requestHash().equals(requestHash)) {
			throw new IdempotencyKeyReusedException();
		}
		if (!stored.completed()) {
			throw new IdempotencyKeyInProgressException();
		}
		log.info("Idempotency-Key repetida com o mesmo pedido: devolvendo a resposta guardada, sem executar de novo");
		return ResponseEntity.status(stored.responseStatus())
				.body(jsonMapper.readValue(stored.responseBody(), responseType));
	}

	private void releaseAfterFailure(UUID merchantId, String key, RuntimeException failure) {
		log.info("O pedido falhou: a Idempotency-Key é liberada para o cliente poder tentar de novo");
		try {
			repository.release(merchantId, key);
		} catch (RuntimeException releaseFailure) {
			// Se nem a liberação funcionar, a chave fica em andamento até expirar. O erro original é o que importa
			// para o cliente e para o log, então é ele que continua sendo lançado.
			failure.addSuppressed(releaseFailure);
		}
	}
}
