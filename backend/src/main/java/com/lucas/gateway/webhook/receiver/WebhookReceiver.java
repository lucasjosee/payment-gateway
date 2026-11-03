package com.lucas.gateway.webhook.receiver;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Faz o papel do servidor do lojista na demonstração. Tudo fica em memória e some quando a API reinicia, inclusive
// o modo "fora do ar", que volta desligado. O mapa só é lido e alterado com o lock deste objeto.
@Component
@Profile("demo")
public class WebhookReceiver {

	// O POST é público e aceita qualquer id na URL, então os três limites juntos seguram a memória: no pior caso,
	// 100 lojistas com 20 corpos de 8 KB, uns 16 MB.
	static final int MAX_BODY_BYTES = 8 * 1024;
	private static final int RECEIVED_PER_MERCHANT = 20;
	private static final int MAX_MERCHANTS = 100;

	private final String baseUrl;

	// Mesmo esquema do MerchantLogStream: passado o limite, sai o lojista usado há mais tempo.
	private final Map<UUID, Inbox> inboxes = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<UUID, Inbox> eldest) {
			return size() > MAX_MERCHANTS;
		}
	};

	public WebhookReceiver(@Value("${gateway.webhook-receiver-base-url}") String baseUrl) {
		this.baseUrl = baseUrl;
	}

	synchronized boolean isOffline(UUID merchantId) {
		Inbox inbox = inboxes.get(merchantId);
		return inbox != null && inbox.offline;
	}

	synchronized void setOffline(UUID merchantId, boolean offline) {
		inboxOf(merchantId).offline = offline;
	}

	synchronized void record(UUID merchantId, ReceivedWebhook webhook) {
		Deque<ReceivedWebhook> received = inboxOf(merchantId).received;
		if (received.size() == RECEIVED_PER_MERCHANT) {
			received.removeLast();
		}
		received.addFirst(webhook);
	}

	synchronized WebhookReceiverResponse describe(UUID merchantId) {
		Inbox inbox = inboxes.get(merchantId);
		boolean offline = inbox != null && inbox.offline;
		List<ReceivedWebhook> received = inbox == null ? List.of() : List.copyOf(inbox.received);
		return new WebhookReceiverResponse(baseUrl + "/" + merchantId, offline, received);
	}

	private Inbox inboxOf(UUID merchantId) {
		return inboxes.computeIfAbsent(merchantId, id -> new Inbox());
	}

	private static final class Inbox {
		private boolean offline;
		private final Deque<ReceivedWebhook> received = new ArrayDeque<>();
	}
}
