package com.lucas.gateway.charge;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface ChargeRepository extends JpaRepository<Charge, UUID> {

	long countByCardFingerprintAndCreatedAtAfter(String cardFingerprint, Instant since);

	Optional<Charge> findByIdAndMerchantId(UUID id, UUID merchantId);
}
