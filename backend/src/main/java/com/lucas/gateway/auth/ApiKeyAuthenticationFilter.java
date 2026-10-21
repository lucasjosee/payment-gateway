package com.lucas.gateway.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

	static final String HEADER = "X-Api-Key";
	public static final String MDC_KEY = "merchantId";

	private final ApiKeyAuthenticator authenticator;

	public ApiKeyAuthenticationFilter(ApiKeyAuthenticator authenticator) {
		this.authenticator = authenticator;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String apiKey = request.getHeader(HEADER);
		if (apiKey != null) {
			authenticator.authenticate(apiKey).ifPresent(merchant -> {
				// Apesar do nome, é a implementação genérica de "usuário autenticado" do Spring Security.
				// Aqui o principal é o lojista, e não há senha nem permissões.
				SecurityContext context = SecurityContextHolder.createEmptyContext();
				context.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(merchant, null, List.of()));
				SecurityContextHolder.setContext(context);
				// Daqui em diante, as linhas de log deste pedido mostram o lojista. Quem tira o valor do MDC no fim
				// do pedido é o RequestIdFilter.
				MDC.put(MDC_KEY, merchant.id().toString());
			});
		}

		// Chave ausente ou inválida não é barrada aqui: a requisição segue como anônima, e quem decide se a rota
		// exige autenticação (e responde 401) são as regras do SecurityConfig.
		filterChain.doFilter(request, response);
	}
}
