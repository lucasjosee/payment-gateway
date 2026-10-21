package com.lucas.gateway.charge;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

	long countByCardFingerprintAndCreatedAtAfter(String cardFingerprint, Instant since);

	Optional<Charge> findByIdAndMerchantId(UUID id, UUID merchantId);

	// O id desempata cobranças criadas no mesmo instante. Sem um critério único, o banco pode devolver as empatadas
	// em ordem diferente a cada consulta, e uma delas apareceria em duas páginas ou em nenhuma.
	Page<Charge> findByMerchantIdOrderByCreatedAtDescIdDesc(UUID merchantId, Pageable pageable);
}
