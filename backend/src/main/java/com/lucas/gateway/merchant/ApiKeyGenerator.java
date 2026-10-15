package com.lucas.gateway.merchant;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class ApiKeyGenerator {

	// A chave é "gw_" seguido de 32 bytes aleatórios em Base64 URL-safe, o que dá 46 caracteres.
	// O prefixo guardado em claro são os 12 primeiros. Mesmo que ele vaze, os mais de 200 bits
	// aleatórios restantes só existem na chave que o lojista recebeu.
	private static final String MARKER = "gw_";
	private static final int RANDOM_BYTES = 32;
	private static final int PREFIX_LENGTH = 12;

	private final SecureRandom secureRandom = new SecureRandom();

	public GeneratedApiKey generate() {
		byte[] randomBytes = new byte[RANDOM_BYTES];
		secureRandom.nextBytes(randomBytes);
		String plainText = MARKER + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
		return new GeneratedApiKey(plainText, plainText.substring(0, PREFIX_LENGTH), sha256Hex(plainText));
	}

	// SHA-256 sem salt é suficiente porque a chave é totalmente aleatória: não existe lista de chaves
	// comuns para testar, diferente de uma senha escolhida por uma pessoa, que pediria bcrypt ou Argon2.
	private static String sha256Hex(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer SHA-256", e);
		}
	}

	public record GeneratedApiKey(String plainText, String prefix, String hash) {

		// O toString padrão de um record imprime todos os campos, e um log deste objeto exporia a chave.
		@Override
		public String toString() {
			return "GeneratedApiKey[prefix=%s]".formatted(prefix);
		}
	}
}
