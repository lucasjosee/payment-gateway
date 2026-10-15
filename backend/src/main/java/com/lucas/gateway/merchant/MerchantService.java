package com.lucas.gateway.merchant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MerchantService {

	private final MerchantRepository merchantRepository;
	private final ApiKeyRepository apiKeyRepository;
	private final ApiKeyGenerator apiKeyGenerator;

	public MerchantService(MerchantRepository merchantRepository, ApiKeyRepository apiKeyRepository,
			ApiKeyGenerator apiKeyGenerator) {
		this.merchantRepository = merchantRepository;
		this.apiKeyRepository = apiKeyRepository;
		this.apiKeyGenerator = apiKeyGenerator;
	}

	@Transactional
	public CreateMerchantResponse register(CreateMerchantRequest request) {
		Merchant merchant = merchantRepository.save(new Merchant(request.name(), request.email()));

		ApiKeyGenerator.GeneratedApiKey key = apiKeyGenerator.generate();
		apiKeyRepository.save(new ApiKey(merchant, key.prefix(), key.hash()));

		return new CreateMerchantResponse(merchant.getId(), merchant.getName(), merchant.getEmail(), key.plainText());
	}
}
