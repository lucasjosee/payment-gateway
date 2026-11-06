package com.lucas.gateway.charge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

	long countByCardFingerprintAndCreatedAtAfter(String cardFingerprint, Instant since);

	Optional<Charge> findByIdAndMerchantId(UUID id, UUID merchantId);

	Optional<Charge> findByIdAndPaymentMethod(UUID id, PaymentMethod paymentMethod);

	// O id desempata cobranças criadas no mesmo instante. Sem um critério único, o banco pode devolver as empatadas
	// em ordem diferente a cada consulta, e uma delas apareceria em duas páginas ou em nenhuma.
	Page<Charge> findByMerchantIdOrderByCreatedAtDescIdDesc(UUID merchantId, Pageable pageable);

	// O status = 'PENDING' escrito por extenso, igual ao do índice parcial da V7, é o que deixa o Postgres usar
	// o índice. O <= é a mesma conta do isPastDeadline da Charge: no instante exato do prazo, o Pix já venceu.
	@Query(value = "SELECT id FROM charges WHERE status = 'PENDING' AND expires_at <= :now", nativeQuery = true)
	List<UUID> findOverduePixIds(Instant now);
}
