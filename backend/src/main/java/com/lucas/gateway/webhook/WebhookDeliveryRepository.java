package com.lucas.gateway.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, UUID> {

	// O id desempata entregas criadas no mesmo instante, pelo mesmo motivo da listagem de cobranças.
	Page<WebhookDelivery> findByMerchantIdOrderByCreatedAtDescIdDesc(UUID merchantId, Pageable pageable);

	Page<WebhookDelivery> findByMerchantIdAndChargeIdOrderByCreatedAtDescIdDesc(UUID merchantId, UUID chargeId,
			Pageable pageable);
}
