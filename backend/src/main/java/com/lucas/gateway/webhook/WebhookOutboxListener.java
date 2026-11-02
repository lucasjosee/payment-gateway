package com.lucas.gateway.webhook;

import com.lucas.gateway.charge.ChargeResponse;
import com.lucas.gateway.charge.ChargeStatus;
import com.lucas.gateway.charge.ChargeStatusChanged;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

// Aqui o webhook não é enviado, só fica guardado como entrega pendente. Quem envia é o despachante, depois.
@Component
public class WebhookOutboxListener {

	private static final Logger log = LoggerFactory.getLogger(WebhookOutboxListener.class);

	private final WebhookEndpointRepository webhookEndpointRepository;
	private final WebhookDeliveryRepository webhookDeliveryRepository;
	private final JsonMapper jsonMapper;

	public WebhookOutboxListener(WebhookEndpointRepository webhookEndpointRepository,
			WebhookDeliveryRepository webhookDeliveryRepository, JsonMapper jsonMapper) {
		this.webhookEndpointRepository = webhookEndpointRepository;
		this.webhookDeliveryRepository = webhookDeliveryRepository;
		this.jsonMapper = jsonMapper;
	}

	// Um @EventListener roda na hora, dentro da transação de quem publicou. A entrega é gravada junto com a
	// mudança de status: se a transação for desfeita, as duas somem, e se ela for confirmada, as duas ficam.
	// O MANDATORY faz o listener falhar se um dia alguém publicar o evento fora de uma transação.
	@EventListener
	@Transactional(propagation = Propagation.MANDATORY)
	public void onChargeStatusChanged(ChargeStatusChanged event) {
		if (!webhookEndpointRepository.existsByMerchantId(event.merchantId())) {
			return;
		}
		ChargeResponse charge = event.charge();
		String type = eventTypeOf(charge.status());
		String payload = jsonMapper.writeValueAsString(new WebhookEvent(type, Instant.now(), charge));
		WebhookDelivery delivery = webhookDeliveryRepository.save(
				new WebhookDelivery(event.merchantId(), charge.id(), type, payload));
		log.info("Webhook {} da cobrança {} ficou pendente na entrega {}", type, charge.id(), delivery.getId());
	}

	// Locale.ROOT porque, com o idioma da máquina em turco, o I de AUTHORIZED viraria um ı sem ponto.
	private static String eventTypeOf(ChargeStatus status) {
		return "charge." + status.name().toLowerCase(Locale.ROOT);
	}
}
