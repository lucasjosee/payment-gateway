package com.lucas.gateway.idempotency;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

// Gera o request_hash da V3__idempotency_keys.sql. Precisa ser HMAC pelo mesmo motivo do card_fingerprint, explicado
// na V2__charges.sql: o corpo de um POST /charges traz o número do cartão e o CVV, e um hash sem chave deixaria
// quem lê o banco testar combinações até achar os dois.
@Component
public class RequestHasher {

	private static final String ALGORITHM = "HmacSHA256";

	private final SecretKeySpec key;
	private final JsonMapper jsonMapper;

	public RequestHasher(@Value("${gateway.idempotency-hash-key}") String key, JsonMapper jsonMapper) {
		this.key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
		this.jsonMapper = jsonMapper;
	}

	// O método e a rota entram junto com o corpo porque as rotas de ação não têm corpo. Sem eles, um /refund com
	// a mesma chave de um /capture pareceria o mesmo pedido e receberia a resposta da captura, sem estornar nada.
	// O corpo entra já convertido em objeto, e não como os bytes recebidos. Assim, dois envios que só mudam espaços
	// ou a ordem dos campos contam como o mesmo pedido.
	public String hash(String method, String path, Object body) {
		String request = method + " " + path + "\n" + jsonMapper.writeValueAsString(body);
		try {
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(key);
			return HexFormat.of().formatHex(mac.doFinal(request.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer HmacSHA256", e);
		}
	}
}
