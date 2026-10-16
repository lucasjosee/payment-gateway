package com.lucas.gateway.merchant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
		// Esta checagem dá um 409 claro no caso comum. Se duas requisições com o mesmo e-mail chegarem
		// ao mesmo tempo, as duas podem passar daqui, e quem barra a segunda é o índice único do banco.
		if (merchantRepository.existsByEmailIgnoreCase(request.email())) {
			throw new EmailAlreadyRegisteredException();
		}

		Merchant merchant = merchantRepository.save(new Merchant(request.name(), request.email()));

		ApiKeyGenerator.GeneratedApiKey key = apiKeyGenerator.generate();
		apiKeyRepository.save(new ApiKey(merchant, key.prefix(), key.hash()));

		return new CreateMerchantResponse(merchant.getId(), merchant.getName(), merchant.getEmail(), key.plainText());
	}

	@Transactional(readOnly = true)
	public MerchantResponse findById(UUID id) {
		// O id vem de uma chave válida, e a chave estrangeira garante que toda chave tem lojista.
		// Não encontrar aqui seria um bug, então o 500 do handler genérico é a resposta certa.
		Merchant merchant = merchantRepository.findById(id).orElseThrow();
		return new MerchantResponse(merchant.getId(), merchant.getName(), merchant.getEmail());
	}
}
