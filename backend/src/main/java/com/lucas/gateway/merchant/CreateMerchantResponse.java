package com.lucas.gateway.merchant;

import java.util.UUID;

// Esta resposta é a única vez que a chave completa sai do sistema. Depois dela, o banco só tem
// o prefixo e o hash, e não há como recuperar a chave: se o lojista perder, precisa de uma nova.
public record CreateMerchantResponse(UUID id, String name, String email, String apiKey) {

	// O toString padrão de um record imprime todos os campos, e um log desta resposta exporia a chave.
	@Override
	public String toString() {
		return "CreateMerchantResponse[id=%s, name=%s, email=%s]".formatted(id, name, email);
	}
}
