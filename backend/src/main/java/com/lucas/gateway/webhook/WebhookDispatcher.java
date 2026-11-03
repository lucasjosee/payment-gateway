package com.lucas.gateway.webhook;

import com.lucas.gateway.auth.ApiKeyAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

// Envia as entregas pendentes que já venceram, uma por transação. A transação segura a trava da linha durante o
// envio, então enquanto ele dura nenhuma outra instância da API pega a mesma entrega.
@Component
public class WebhookDispatcher {

	private static final Logger log = LoggerFactory.getLogger(WebhookDispatcher.class);

	private final WebhookDeliveryRepository webhookDeliveryRepository;
	private final WebhookEndpointRepository webhookEndpointRepository;
	private final WebhookSigningSecrets signingSecrets;
	private final WebhookUrlValidator urlValidator;
	private final WebhookSender sender;
	private final TransactionTemplate transactionTemplate;
	private final List<Duration> retryDelays;

	public WebhookDispatcher(WebhookDeliveryRepository webhookDeliveryRepository,
			WebhookEndpointRepository webhookEndpointRepository, WebhookSigningSecrets signingSecrets,
			WebhookUrlValidator urlValidator, WebhookSender sender, TransactionTemplate transactionTemplate,
			@Value("${gateway.webhook-retry-delays}") List<Duration> retryDelays) {
		this.webhookDeliveryRepository = webhookDeliveryRepository;
		this.webhookEndpointRepository = webhookEndpointRepository;
		this.signingSecrets = signingSecrets;
		this.urlValidator = urlValidator;
		this.sender = sender;
		this.transactionTemplate = transactionTemplate;
		this.retryDelays = retryDelays;
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
	// ainda está pendente. O id da entrega vai como id do evento porque é o mesmo em toda tentativa, e é por ele que
	// o lojista reconhece um evento que já recebeu.
	private void send(WebhookDelivery delivery) {
		WebhookEndpoint endpoint = webhookEndpointRepository.findByMerchantId(delivery.getMerchantId()).orElseThrow();
		String signingSecret = signingSecrets.secretFor(endpoint.getId());
		log.info("Enviando o webhook {} da cobrança {}, tentativa {} de {}", delivery.getEventType(),
				delivery.getChargeId(), delivery.getAttempts() + 1, retryDelays.size() + 1);
		long start = System.nanoTime();
		try {
			// De novo a cada envio, e não só no cadastro, porque o host pode passar a resolver para outro endereço.
			urlValidator.validate(delivery.getMerchantId(), endpoint.getUrl());
			int httpStatus = sender.send(endpoint.getUrl(), delivery.getId(), signingSecret, delivery.getPayload());
			long elapsedMs = (System.nanoTime() - start) / 1_000_000;
			if (httpStatus >= 200 && httpStatus < 300) {
				delivery.markDelivered(httpStatus);
				log.info("Webhook {} entregue: o servidor do lojista respondeu {} em {} ms", delivery.getEventType(),
						httpStatus, elapsedMs);
			} else {
				log.warn("Webhook {} falhou: o servidor do lojista respondeu {} em {} ms", delivery.getEventType(),
						httpStatus, elapsedMs);
				retryOrGiveUp(delivery, httpStatus);
			}
		} catch (InvalidWebhookUrlException e) {
			log.warn("Webhook {} não foi enviado: {}", delivery.getEventType(), e.getMessage());
			retryOrGiveUp(delivery, null);
		} catch (RestClientException | IllegalArgumentException e) {
			log.warn("Webhook {} falhou sem resposta do servidor do lojista: {} em {} ms", delivery.getEventType(),
					reasonOf(e), (System.nanoTime() - start) / 1_000_000);
			retryOrGiveUp(delivery, null);
		}
	}

	// A espera depois da primeira falha é a primeira da lista, e assim por diante. Quando a lista acaba, a entrega
	// desiste: com cinco esperas, são seis tentativas.
	private void retryOrGiveUp(WebhookDelivery delivery, Integer httpStatus) {
		int failures = delivery.getAttempts() + 1;
		if (failures <= retryDelays.size()) {
			Duration delay = retryDelays.get(failures - 1);
			delivery.scheduleRetry(httpStatus, delay);
			log.info("Nova tentativa do webhook {} em {} s", delivery.getEventType(), delay.toSeconds());
		} else {
			delivery.markFailed(httpStatus);
			log.warn("Webhook {} sem novas tentativas: a entrega ficou FAILED depois de {} tentativas",
					delivery.getEventType(), delivery.getAttempts());
		}
	}

	// A causa direta diz o que houve, como ConnectException ou HttpTimeoutException. Vai só o nome dela porque a
	// mensagem costuma trazer a URL inteira, e o lojista pode ter posto um token nela.
	private static String reasonOf(Exception e) {
		Throwable cause = e.getCause() != null ? e.getCause() : e;
		return cause.getClass().getSimpleName();
	}
}
