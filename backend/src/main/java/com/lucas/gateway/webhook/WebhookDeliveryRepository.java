package com.lucas.gateway.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

	// SQL nativo para o SKIP LOCKED ficar à vista, em vez de escondido numa dica de lock do Hibernate. O
	// status = 'PENDING' escrito por extenso, igual ao do índice parcial da V6, é o que deixa o Postgres usar o índice.
	// O \ no fim das linhas junta a consulta numa linha só, e o log do profile demo, que a mostra a cada segundo,
	// ganha uma linha por consulta em vez de cinco.
	@Query(value = """
			SELECT * FROM webhook_deliveries \
			WHERE status = 'PENDING' AND next_attempt_at <= :now \
			ORDER BY next_attempt_at \
			LIMIT 1 \
			FOR UPDATE SKIP LOCKED""", nativeQuery = true)
	Optional<WebhookDelivery> findNextDueForUpdate(Instant now);

	// O id desempata entregas criadas no mesmo instante, pelo mesmo motivo da listagem de cobranças.
	Page<WebhookDelivery> findByMerchantIdOrderByCreatedAtDescIdDesc(UUID merchantId, Pageable pageable);

	Page<WebhookDelivery> findByMerchantIdAndChargeIdOrderByCreatedAtDescIdDesc(UUID merchantId, UUID chargeId,
			Pageable pageable);
}
