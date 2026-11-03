package com.lucas.gateway.webhook;

import com.lucas.gateway.auth.ApiKeyAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Instant;
import java.util.Optional;

// Envia as entregas pendentes que já venceram, uma por transação. A transação segura a trava da linha durante o
// envio, então enquanto ele dura nenhuma outra instância da API pega a mesma entrega.
@Component
public class WebhookDispatcher {

	private static final Logger log = LoggerFactory.getLogger(WebhookDispatcher.class);

	private final WebhookDeliveryRepository webhookDeliveryRepository;
	private final WebhookEndpointRepository webhookEndpointRepository;
	private final WebhookSender sender;
	private final TransactionTemplate transactionTemplate;

	public WebhookDispatcher(WebhookDeliveryRepository webhookDeliveryRepository,
			WebhookEndpointRepository webhookEndpointRepository, WebhookSender sender,
			TransactionTemplate transactionTemplate) {
		this.webhookDeliveryRepository = webhookDeliveryRepository;
		this.webhookEndpointRepository = webhookEndpointRepository;
		this.sender = sender;
		this.transactionTemplate = transactionTemplate;
	}

	// O fixedDelay conta a partir do fim da rodada anterior, então duas rodadas desta instância nunca se sobrepõem.
	@Scheduled(fixedDelay = 1000)
	public void dispatchDue() {
		boolean dispatched;
		do {
			try {
				dispatched = Boolean.TRUE.equals(transactionTemplate.execute(status -> dispatchNext()));
			} finally {
				// O lojista sai do MDC só depois do commit, para o UPDATE da entrega também ir para o console dele.
				MDC.remove(ApiKeyAuthenticationFilter.MDC_KEY);
			}
		} while (dispatched);
	}

	private boolean dispatchNext() {
		Optional<WebhookDelivery> next = webhookDeliveryRepository.findNextDueForUpdate(Instant.now());
		if (next.isEmpty()) {
			return false;
		}
		WebhookDelivery delivery = next.get();
		// A mesma chave que o filtro da API key usa, para as linhas do envio irem para o console do lojista.
		MDC.put(ApiKeyAuthenticationFilter.MDC_KEY, delivery.getMerchantId().toString());
		send(delivery);
		return true;
	}

	// O endpoint é lido agora, e não quando a entrega foi criada, para uma troca de URL valer também para o que
	// ainda está pendente.
	private void send(WebhookDelivery delivery) {
		String url = webhookEndpointRepository.findByMerchantId(delivery.getMerchantId()).orElseThrow().getUrl();
		log.info("Enviando o webhook {} da cobrança {}", delivery.getEventType(), delivery.getChargeId());
		long start = System.nanoTime();
		try {
			int httpStatus = sender.send(url, delivery.getPayload());
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			if (httpStatus >= 200 && httpStatus < 300) {
				delivery.markDelivered(httpStatus);
				log.info("Webhook {} entregue: o servidor do lojista respondeu {} em {} ms", delivery.getEventType(),
						httpStatus, elapsedMs);
			} else {
				delivery.markFailed(httpStatus);
				log.warn("Webhook {} falhou: o servidor do lojista respondeu {} em {} ms", delivery.getEventType(),
						httpStatus, elapsedMs);
			}
		} catch (RestClientException | IllegalArgumentException e) {
			delivery.markFailed(null);
			log.warn("Webhook {} falhou sem resposta do servidor do lojista: {} em {} ms", delivery.getEventType(),
					reasonOf(e), (System.nanoTime() - start) / 1_000_000);
		}
	}

	// A causa direta diz o que houve, como ConnectException ou HttpTimeoutException. Vai só o nome dela porque a
	// mensagem costuma trazer a URL inteira, e o lojista pode ter posto um token nela.
	private static String reasonOf(Exception e) {
		Throwable cause = e.getCause() != null ? e.getCause() : e;
		return cause.getClass().getSimpleName();
	}
}
