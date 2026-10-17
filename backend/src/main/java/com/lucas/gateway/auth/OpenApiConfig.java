package com.lucas.gateway.auth;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String API_KEY_SCHEME = "apiKey";

	@Bean
	public OpenAPI openApi() {
		SecurityScheme apiKey = new SecurityScheme()
				.type(SecurityScheme.Type.APIKEY)
				.in(SecurityScheme.In.HEADER)
				.name(ApiKeyAuthenticationFilter.HEADER);

		return new OpenAPI()
				.info(new Info()
						.title("Gateway de Pagamento")
						.version("v0")
						.description("Para usar as rotas com cadeado, cadastre um lojista em POST /merchants, "
								+ "copie a apiKey da resposta e cole em Authorize."))
				.components(new Components().addSecuritySchemes(API_KEY_SCHEME, apiKey))
				// Assim como no SecurityConfig, toda rota nasce exigindo a chave. As públicas são marcadas no
				// controller com @SecurityRequirements vazio e precisam ser as mesmas liberadas no SecurityConfig.
				.addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME));
	}
}
