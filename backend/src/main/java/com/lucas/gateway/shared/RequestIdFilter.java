package com.lucas.gateway.shared;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

// Como @Component, o Spring Boot registra este filtro direto no Tomcat, fora do Spring Security, e a ordem o põe
// antes de todos os outros. Assim até um 401 sai com o ID e com a linha de log.
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

	static final String HEADER = "X-Request-Id";
	static final String MDC_KEY = "requestId";

	// O ID recebido vai parar nas linhas de log. Sem esta restrição, um cliente poderia mandar uma quebra de linha
	// seguida de uma linha de log inventada, ou um texto enorme que se repetiria em todas as linhas do pedido.
	private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

	private static final Logger log = LoggerFactory.getLogger(RequestIdFilter.class);

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String requestId = requestIdOf(request);
		MDC.put(MDC_KEY, requestId);
		response.setHeader(HEADER, requestId);

		long start = System.nanoTime();
		try {
			filterChain.doFilter(request, response);
			log.info("{} {} -> {} ({} ms)", request.getMethod(), request.getRequestURI(), response.getStatus(),
					millisSince(start));
		} catch (IOException | ServletException | RuntimeException ex) {
			// Uma exceção que chega até aqui escapou de todos os handlers, como uma falha do banco durante a
			// autenticação. A resposta só é decidida depois, quando o Tomcat encaminha o pedido para a página de erro,
			// e a essa altura o MDC já foi limpo. Esta é a única linha desse erro que sai com o ID do pedido.
			log.error("{} {} -> exceção não tratada ({} ms)", request.getMethod(), request.getRequestURI(),
					millisSince(start), ex);
			throw ex;
		} finally {
			// As threads do Tomcat atendem um pedido atrás do outro. Sem limpar, o próximo pedido desta thread
			// herdaria o ID e o lojista deste nos logs. O clear apaga também o lojista, que o filtro de API key põe.
			MDC.clear();
		}
	}

	private static long millisSince(long startNanos) {
		return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
	}

	private static String requestIdOf(HttpServletRequest request) {
		String received = request.getHeader(HEADER);
		if (received != null && SAFE_ID.matcher(received).matches()) {
			return received;
		}
		return UUID.randomUUID().toString();
	}
}
