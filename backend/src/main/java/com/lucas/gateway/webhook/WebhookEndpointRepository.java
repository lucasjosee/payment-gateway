package com.lucas.gateway.webhook;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WebhookEndpointRepository extends JpaRepository<WebhookEndpoint, UUID> {

	Optional<WebhookEndpoint> findByMerchantId(UUID merchantId);
}
