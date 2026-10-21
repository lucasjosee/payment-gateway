package com.lucas.gateway.devlogs;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Guarda as últimas linhas de log de cada lojista e repassa as novas, na hora, para as conexões abertas por ele.
// Os dois mapas só são lidos e alterados com o lock deste objeto.
@Component
@Profile("demo")
public class MerchantLogStream {

	private static final int LINES_PER_MERCHANT = 200;

	// Qualquer pessoa pode cadastrar lojistas, então sem um teto a memória cresceria a cada cadastro. Passado o
	// limite, sai o histórico do lojista que está há mais tempo sem gerar log.
	private static final int MAX_MERCHANTS = 100;

	// Depois disso a conexão é encerrada e o cliente precisa abrir outra. Sem prazo, a conexão de um cliente que sumiu
	// sem avisar só seria descoberta quando o lojista dele voltasse a gerar log.
	private static final Duration CONNECTION_TIMEOUT = Duration.ofMinutes(30);

	// Com accessOrder = true, cada leitura ou escrita leva o lojista para o fim, e o removeEldestEntry descarta o do
	// começo, que é o usado há mais tempo.
	private final Map<UUID, Deque<LogLine>> recentLines = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<UUID, Deque<LogLine>> eldest) {
			return size() > MAX_MERCHANTS;
		}
	};

	private final Map<UUID, Set<SseEmitter>> subscribers = new HashMap<>();

	void publish(UUID merchantId, LogLine line) {
		List<SseEmitter> targets;
		synchronized (this) {
			Deque<LogLine> lines = recentLines.computeIfAbsent(merchantId, id -> new ArrayDeque<>());
			if (lines.size() == LINES_PER_MERCHANT) {
				lines.removeFirst();
			}
			lines.addLast(line);
			targets = List.copyOf(subscribers.getOrDefault(merchantId, Set.of()));
		}

		// O envio fica fora do lock porque escrever numa conexão lenta pode demorar, e os pedidos dos outros lojistas
		// não podem ficar esperando para registrar o próprio log.
		for (SseEmitter emitter : targets) {
			send(emitter, line);
		}
	}

	SseEmitter subscribe(UUID merchantId) {
		SseEmitter emitter = new SseEmitter(CONNECTION_TIMEOUT.toMillis());
		emitter.onCompletion(() -> unsubscribe(merchantId, emitter));
		emitter.onError(error -> unsubscribe(merchantId, emitter));
		emitter.onTimeout(emitter::complete);

		// O histórico vai junto com o registro, dentro do lock, para nenhuma linha nova passar na frente dele nem
		// aparecer duas vezes. Isso não segura os outros pedidos: enquanto o controller não devolve o emitter, o Spring
		// só guarda os envios em memória, sem escrever nada na conexão.
		synchronized (this) {
			for (LogLine line : recentLines.getOrDefault(merchantId, new ArrayDeque<>())) {
				send(emitter, line);
			}
			subscribers.computeIfAbsent(merchantId, id -> new HashSet<>()).add(emitter);
		}
		return emitter;
	}

	private synchronized void unsubscribe(UUID merchantId, SseEmitter emitter) {
		Set<SseEmitter> emitters = subscribers.get(merchantId);
		if (emitters != null) {
			emitters.remove(emitter);
			if (emitters.isEmpty()) {
				subscribers.remove(merchantId);
			}
		}
	}

	private static void send(SseEmitter emitter, LogLine line) {
		try {
			emitter.send(line);
		} catch (IOException | IllegalStateException closed) {
			// A conexão caiu ou já foi encerrada. O Spring avisa pelo onError ou pelo onCompletion, que tiram o emitter
			// da lista. Registrar a falha no log não é uma opção: a linha nova voltaria para este mesmo método.
		}
	}
}
