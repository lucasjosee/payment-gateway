package com.lucas.gateway.auth;

import com.lucas.gateway.merchant.ApiKey;
import com.lucas.gateway.merchant.ApiKeyGenerator;
import com.lucas.gateway.merchant.ApiKeyRepository;
import com.lucas.gateway.merchant.AuthenticatedMerchant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

@Service
public class ApiKeyAuthenticator {

	private final ApiKeyRepository apiKeyRepository;

	public ApiKeyAuthenticator(ApiKeyRepository apiKeyRepository) {
		this.apiKeyRepository = apiKeyRepository;
	}

	@Transactional(readOnly = true)
	public Optional<AuthenticatedMerchant> authenticate(String plainText) {
		return ApiKeyGenerator.prefixOf(plainText)
				.flatMap(apiKeyRepository::findByPrefix)
				.filter(apiKey -> hashMatches(apiKey, plainText))
				.map(apiKey -> new AuthenticatedMerchant(apiKey.getMerchant().getId()));
	}

	// MessageDigest.isEqual leva o mesmo tempo não importa onde os hashes comecem a diferir. Um equals comum
	// pararia no primeiro caractere diferente, e o tempo de resposta daria pistas para quem está testando chaves.
	private static boolean hashMatches(ApiKey apiKey, String plainText) {
		byte[] stored = apiKey.getKeyHash().getBytes(StandardCharsets.UTF_8);
		byte[] received = ApiKeyGenerator.hashOf(plainText).getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(stored, received);
	}
}
