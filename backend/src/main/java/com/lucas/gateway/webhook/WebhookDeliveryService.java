package com.lucas.gateway.webhook;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class WebhookDeliveryService {

	private final WebhookDeliveryRepository webhookDeliveryRepository;

	public WebhookDeliveryService(WebhookDeliveryRepository webhookDeliveryRepository) {
		this.webhookDeliveryRepository = webhookDeliveryRepository;
	}

	// Com o filtro por cobrança, o lojista continua na consulta. O id de uma cobrança de outro lojista dá uma
	// lista vazia, igual à de um id que não existe.
	@Transactional(readOnly = true)
	public WebhookDeliveryPage list(UUID merchantId, UUID chargeId, int page, int size) {
		PageRequest pageRequest = PageRequest.of(page, size);
		Page<WebhookDelivery> deliveries = chargeId == null
				? webhookDeliveryRepository.findByMerchantIdOrderByCreatedAtDescIdDesc(merchantId, pageRequest)
				: webhookDeliveryRepository.findByMerchantIdAndChargeIdOrderByCreatedAtDescIdDesc(merchantId, chargeId,
						pageRequest);
		Page<WebhookDeliverySummary> summaries = deliveries.map(this::toSummary);
		return new WebhookDeliveryPage(summaries.getContent(), summaries.getNumber(), summaries.getSize(),
				summaries.getTotalElements(), summaries.getTotalPages());
	}

	private WebhookDeliverySummary toSummary(WebhookDelivery delivery) {
		return new WebhookDeliverySummary(delivery.getId(), delivery.getChargeId(), delivery.getEventType(),
				delivery.getStatus(), delivery.getAttempts(), delivery.getNextAttemptAt(),
				delivery.getLastHttpStatus(), delivery.getCreatedAt());
	}
}
