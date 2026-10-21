package com.lucas.gateway.auth;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, ApiKeyAuthenticator authenticator,
			@Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) throws Exception {
		http
				// CSRF protege sessões guardadas em cookie, que o navegador envia sozinho. Esta API não usa cookie
				// nem sessão: a chave vai num header que só é enviado de propósito, então não há o que proteger.
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// As rotas são comparadas já sem o context-path do application.yaml, então "/merchants" vale para
				// /api/merchants. Escrever "/api/merchants" aqui faria a regra nunca casar.
				.authorizeHttpRequests(routes -> routes
						.requestMatchers(HttpMethod.POST, "/merchants").permitAll()
						.requestMatchers("/actuator/health/**").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
						.anyRequest().authenticated())
				// Erros do Spring Security acontecem nos filtros, antes de chegar a qualquer controller, e por isso
				// não passam pelo ApiExceptionHandler. Encaminhar para o resolver faz o 401 sair em Problem Details.
				.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
						(request, response, exception) -> exceptionResolver.resolveException(request, response, null, exception)))
				// O filtro é criado aqui, e não como @Component, de propósito: o Spring Boot registra todo Filter que é
				// bean também fora do Spring Security, e ele rodaria duas vezes em cada requisição.
				.addFilterBefore(new ApiKeyAuthenticationFilter(authenticator), AnonymousAuthenticationFilter.class);
		return http.build();
	}
}
