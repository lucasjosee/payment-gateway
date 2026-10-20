package com.lucas.gateway.charge;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.HexFormat;

// Gera o card_fingerprint da V2__charges.sql. O comentário de lá explica por que precisa ser HMAC, e não um hash simples.
@Component
public class CardFingerprinter {

	private static final String ALGORITHM = "HmacSHA256";

	private final SecretKeySpec key;

	public CardFingerprinter(@Value("${gateway.card-fingerprint-key}") String key) {
		this.key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
	}

	public String fingerprintOf(String cardNumber) {
		try {
			// Um Mac guarda estado entre chamadas e não pode ser usado por duas threads ao mesmo tempo.
			// Criar um por chamada é barato e evita esse problema.
			Mac mac = Mac.getInstance(ALGORITHM);
			mac.init(key);
			return HexFormat.of().formatHex(mac.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8)));
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Toda JVM é obrigada a oferecer HmacSHA256", e);
		}
	}
}
