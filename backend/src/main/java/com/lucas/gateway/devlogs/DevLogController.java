package com.lucas.gateway.devlogs;

import com.lucas.gateway.merchant.AuthenticatedMerchant;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Desenvolvimento")
@Profile("demo")
@RestController
@RequestMapping("/dev/logs")
public class DevLogController {

	private final MerchantLogStream stream;

	public DevLogController(MerchantLogStream stream) {
		this.stream = stream;
	}

	// Primeiro chegam as últimas linhas guardadas e depois as novas, conforme acontecem. O lojista vem da API key, e
	// não de um parâmetro, então não há como pedir as linhas de outro lojista.
	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@AuthenticationPrincipal AuthenticatedMerchant merchant) {
		return stream.subscribe(merchant.id());
	}
}
